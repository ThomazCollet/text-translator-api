package com.thomazcollet.text_translator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.dtos.VoiceRSSRequest;

@Service
public class TextToSpeechService {

    private final RestTemplate restTemplate;
    private final String url;
    private final String apiKey;

    public TextToSpeechService(RestTemplate restTemplate, @Value("${voicerss.api.url}") String url,
            @Value("${voicerss.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.apiKey = apiKey;
    }

    public SpeechResponse speech(SpeechRequest request) {

        // 1. Montamos a URL com os parâmetros grudados nela do jeito que a API gosta
        String urlCompleta = String.format(
                "%s?key=%s&src=%s&hl=%s&c=%s&f=%s&b64=%s",
                url,
                apiKey,
                request.textToSpeak(),
                request.targetLanguage().getVoiceRssCode(),
                "MP3",
                "22kHz_16bit_mono",
                "true");
        // 2. Chamada à API externa (Passamos null no meio porque não tem corpo JSON!)
        ResponseEntity<String> response = restTemplate.postForEntity(urlCompleta, null, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Erro ao chamar API de áudio VoiceRSS");
        }

        // 3. Retorna a DTO interna de resposta
        return new SpeechResponse(
                response.getBody(),
                request.textToSpeak(),
                request.targetLanguage());
    }
}
