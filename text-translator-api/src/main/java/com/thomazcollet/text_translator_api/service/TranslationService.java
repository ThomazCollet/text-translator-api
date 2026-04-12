package com.thomazcollet.text_translator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.thomazcollet.text_translator_api.dtos.LibreTranslateRequest;
import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.enums.Language;

@Service
public class TranslationService {

    private final RestTemplate restTemplate;

    private final String url;

    public TranslationService(RestTemplate restTemplate, @Value("${translation.api.url}") String url) {

        this.restTemplate = restTemplate;

        this.url = url;

    }

    // 1. O PORTEIRO (Interface única com o Controller)
    public TextResponse translate(TextRequest request) {
        if (needsBridge(request)) {
            return bridgeTranslate(request);
        }
        return simpleTranslate(request);
    }

    // 2. LÓGICA DE DECISÃO (Privada, limpa o porteiro)
    private boolean needsBridge(TextRequest request) {
        return request.sourceLanguage() != Language.AUTO
                && request.sourceLanguage() != Language.EN
                && request.targetLanguage() != Language.EN;
    }

    // 3. TRADUÇÃO SIMPLES (Direta ou com Auto-detect)
    private TextResponse simpleTranslate(TextRequest request) {
        LibreTranslateResponse response = callApi(
                request.text(),
                request.sourceLanguage().getLibreCode(),
                request.targetLanguage().getLibreCode());

        return buildResponse(request, response);
    }

    // 4. TRADUÇÃO EM PONTE (PT -> EN -> ES)
    private TextResponse bridgeTranslate(TextRequest request) {
        // Passo 1: Origem -> Inglês
        LibreTranslateResponse firstStep = callApi(request.text(), request.sourceLanguage().getLibreCode(), "en");

        // Passo 2: Inglês -> Destino
        LibreTranslateResponse secondStep = callApi(firstStep.translatedText(), "en",
                request.targetLanguage().getLibreCode());

        return buildResponse(request, secondStep);
    }

    // 5. O TRABALHADOR BRAÇAL (Comunicação com a API)
    private LibreTranslateResponse callApi(String text, String source, String target) {
        LibreTranslateRequest body = new LibreTranslateRequest(text, source, target);
        HttpEntity<LibreTranslateRequest> entity = new HttpEntity<>(body, createHeaders());

        ResponseEntity<LibreTranslateResponse> response = restTemplate.postForEntity(url, entity,
                LibreTranslateResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Erro na API externa");
        }
        return response.getBody();
    }

    // Métodos auxiliares para manter tudo limpo
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private TextResponse buildResponse(TextRequest request, LibreTranslateResponse response) {
        Language sourceLanguage = (request.sourceLanguage() == Language.AUTO)
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
