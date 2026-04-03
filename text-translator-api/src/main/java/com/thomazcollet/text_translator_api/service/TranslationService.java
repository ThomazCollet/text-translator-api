package com.thomazcollet.text_translator_api.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.LibreTranslateRequest;
import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;

@Service
public class TranslationService {

    private final RestTemplate restTemplate;

    public TranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TextResponse translate(TextRequest request) {

        // Adapta o DTO interno para o formato esperado pela API externa
        LibreTranslateRequest body = new LibreTranslateRequest(
                request.text(),
                request.sourceLanguage().getCode(),
                request.targetLanguage().getCode());

        // Chamada à API de tradução
        String url = "https://libretranslate.com/translate";

        ResponseEntity<LibreTranslateResponse> response = restTemplate.postForEntity(
                url,
                body,
                LibreTranslateResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Erro ao chamar API de tradução");
        }

        // Converte resposta da API externa para o DTO interno da aplicação
        return new TextResponse(
            request.text(),
            response.getBody().translatedText(),
            request.sourceLanguage(),
            request.targetLanguage(),
            null // Audio ainda não capturado
        );
    }
}
