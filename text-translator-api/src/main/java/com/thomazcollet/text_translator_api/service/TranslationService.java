package com.thomazcollet.text_translator_api.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.LibreTranslateRequest;
import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.exception.ExternalApiException;
import com.thomazcollet.text_translator_api.exception.InvalidTextException;
import com.thomazcollet.text_translator_api.exception.TranslationBridgeException;

/**
 * Serviço de orquestração de traduções.
 * Implementa cache distribuído e estratégia de tradução em ponte para idiomas periféricos.
 */
@Service
public class TranslationService {

    private static final String CACHE_NAME = "translations";
    
    private final RestTemplate restTemplate;
    private final String url;

    public TranslationService(RestTemplate restTemplate, @Value("${translation.api.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    /**
     * Realiza a tradução de um texto. 
     * Utiliza Redis para cachear resultados e evitar chamadas repetitivas à API externa.
     */
    @Cacheable(value = CACHE_NAME, key = "{ #request.text().toLowerCase().trim(), #request.sourceLanguage(), #request.targetLanguage() }", unless = "#result == null")
    public TextResponse translate(TextRequest request) {
        validateRequest(request);

        return needsBridge(request) 
            ? bridgeTranslate(request) 
            : simpleTranslate(request);
    }

    private void validateRequest(TextRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            throw new InvalidTextException();
        }
    }

    private boolean needsBridge(TextRequest request) {
        return request.sourceLanguage() != Language.AUTO
                && request.sourceLanguage() != Language.EN
                && request.targetLanguage() != Language.EN;
    }

    private TextResponse simpleTranslate(TextRequest request) {
        var response = callApi(
                request.text(),
                request.sourceLanguage().getLibreCode(),
                request.targetLanguage().getLibreCode());

        return buildResponse(request, response);
    }

    private TextResponse bridgeTranslate(TextRequest request) {
        try {
            // Estágio 1: Origem -> Inglês
            var toEnglish = callApi(request.text(), request.sourceLanguage().getLibreCode(), Language.EN.getLibreCode());
            
            // Estágio 2: Inglês -> Destino
            var toTarget = callApi(toEnglish.translatedText(), Language.EN.getLibreCode(), request.targetLanguage().getLibreCode());

            return buildResponse(request, toTarget);
        } catch (ExternalApiException e) {
            throw new TranslationBridgeException("Falha na orquestração da tradução via ponte (EN).", e);
        }
    }

    /**
     * Comunicação direta com o servidor de tradução.
     */
    private LibreTranslateResponse callApi(String text, String source, String target) {
        try {
            var body = new LibreTranslateRequest(text, source, target);
            var entity = new HttpEntity<>(body, createHeaders());

            ResponseEntity<LibreTranslateResponse> response = restTemplate.postForEntity(url, entity, LibreTranslateResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("API externa retornou status: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (Exception e) {
            throw new ExternalApiException("Erro de conectividade com o serviço de tradução.", e);
        }
    }

    private HttpHeaders createHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private TextResponse buildResponse(TextRequest request, LibreTranslateResponse apiResponse) {
        // Resolve o idioma de origem: usa o detectado pela API se o original foi AUTO
        Language resolvedSource = (request.sourceLanguage() == Language.AUTO)
                ? Optional.ofNullable(apiResponse.detectedLanguage())
                          .map(dl -> Language.fromLibreCode(dl.language()))
                          .orElse(Language.AUTO)
                : request.sourceLanguage();

        return new TextResponse(
                request.text(),
                apiResponse.translatedText(),
                resolvedSource,
                request.targetLanguage(),
                null // Áudio é processado em serviço/endpoint separado
        );
    }
}