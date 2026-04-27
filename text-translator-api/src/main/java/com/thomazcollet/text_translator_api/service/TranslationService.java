package com.thomazcollet.text_translator_api.service;

import java.util.Collections;

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
 * Serviço responsável pela orquestração de traduções de texto.
 * Gerencia traduções diretas e fluxos complexos em ponte para garantir a
 * qualidade do resultado.
 */
@Service
public class TranslationService {

    private final RestTemplate restTemplate;
    private final String url;

    public TranslationService(RestTemplate restTemplate, @Value("${translation.api.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    /**
     * Ponto de entrada principal para solicitações de tradução.
     * Aplica o padrão Fail-Fast para validar a integridade dos dados de entrada.
     */
    @Cacheable(value = "translations", key = "{ #request.text().toLowerCase().trim(), #request.sourceLanguage(), #request.targetLanguage() }", unless = "#result == null")
    public TextResponse translate(TextRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            throw new InvalidTextException();
        }

        return needsBridge(request) ? bridgeTranslate(request) : simpleTranslate(request);
    }

    /**
     * Identifica se a tradução requer um idioma intermediário (Inglês) para maior
     * precisão.
     */
    private boolean needsBridge(TextRequest request) {
        return request.sourceLanguage() != Language.AUTO
                && request.sourceLanguage() != Language.EN
                && request.targetLanguage() != Language.EN;
    }

    private TextResponse simpleTranslate(TextRequest request) {
        final var response = callApi(
                request.text(),
                request.sourceLanguage().getLibreCode(),
                request.targetLanguage().getLibreCode());

        return buildResponse(request, response);
    }

    /**
     * Executa a estratégia de tradução em dois estágios (Origem -> EN -> Destino).
     * Garante a rastreabilidade do erro caso um dos estágios falhe.
     */
    private TextResponse bridgeTranslate(TextRequest request) {
        try {
            final var firstStep = callApi(request.text(), request.sourceLanguage().getLibreCode(), "en");
            final var secondStep = callApi(firstStep.translatedText(), "en", request.targetLanguage().getLibreCode());

            return buildResponse(request, secondStep);
        } catch (ExternalApiException e) {
            throw new TranslationBridgeException("Falha na orquestração da tradução em dois estágios.", e);
        }
    }

    /**
     * Realiza a comunicação de baixo nível com a infraestrutura da API externa.
     * 
     * @throws ExternalApiException em caso de falha de conectividade ou erro do
     *                              servidor remoto.
     */
    private LibreTranslateResponse callApi(String text, String source, String target) {
        try {
            final var body = new LibreTranslateRequest(text, source, target);
            final var entity = new HttpEntity<>(body, createHeaders());

            ResponseEntity<LibreTranslateResponse> response = restTemplate.postForEntity(url, entity,
                    LibreTranslateResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException(
                        "A API de tradução retornou um status inesperado: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (Exception e) {
            throw new ExternalApiException("Falha crítica na comunicação com o serviço de tradução.", e);
        }
    }

    private HttpHeaders createHeaders() {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private TextResponse buildResponse(TextRequest request, LibreTranslateResponse response) {
        final var sourceLanguage = (request.sourceLanguage() == Language.AUTO)
                ? Language.fromLibreCode(response.detectedLanguage().language())
                : request.sourceLanguage();

        return new TextResponse(
                request.text(),
                response.translatedText(),
                sourceLanguage,
                request.targetLanguage(),
                null);
    }
}