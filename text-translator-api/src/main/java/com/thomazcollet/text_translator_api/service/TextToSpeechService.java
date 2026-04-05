package com.thomazcollet.text_translator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.dtos.VoiceRSSRequest;



@Service
public class TextToSpeechService {
    
    private final RestTemplate restTemplate;
    private final String url;
    private final String apiKey;

    public TextToSpeechService(RestTemplate restTemplate, @Value("${voicerss.api.url}") String url, @Value("${voicerss.api.key}")String apiKey){
        this.restTemplate = restTemplate;
        this.url = url;
        this.apiKey = apiKey;
    }

    public SpeechResponse speech(SpeechRequest request){

        // Adapta o DTO interno para o formato esperado pela API externa
        VoiceRSSRequest voiceRSSRequest = new VoiceRSSRequest(
            apiKey,
            request.textToSpeak(),
            request.targetLanguage().getVoiceRssCode()
        );

        // Chamada à API externa
        ResponseEntity<String> response = restTemplate.postForEntity(url, voiceRSSRequest, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null){
            throw new RuntimeException("Erro ao chamar API de áudio VoiceRSS");
        }

        // Retorna dto interna de resposta
        return new SpeechResponse(
            response.getBody(),
            request.textToSpeak(),
            request.targetLanguage()
        );
    }
}
