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

    public TextResponse translate(TextRequest request) {

        // Adapta o DTO interno para o formato esperado pela API externa
        LibreTranslateRequest body = new LibreTranslateRequest(
                request.text(),
                request.sourceLanguage().getLibreCode(),
                request.targetLanguage().getLibreCode());

        // Chamada à API de tradução
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LibreTranslateRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<LibreTranslateResponse> response = restTemplate.postForEntity(url, entity,
                LibreTranslateResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            throw new RuntimeException("Erro ao chamar API de tradução");
        }

        System.out.println("JSON Bruto da API: " + response.getBody());

        Language sourceLanguage = (request.sourceLanguage() == Language.AUTO)
        ? Language.fromLibreCode(response.getBody().detectedLanguage().language()) // Adicionado .language()
        : request.sourceLanguage();

        // Converte resposta da API externa para o DTO interno da aplicação
        return new TextResponse(
                request.text(),
                response.getBody().translatedText(),
                sourceLanguage,
                request.targetLanguage(),
                null // Audio ainda não capturado
        );
    }
}
