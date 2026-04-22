package com.thomazcollet.text_translator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.exception.TextToSpeechLimitException;

/**
 * Serviço de alta disponibilidade para conversão de texto em fala (TTS).
 * Implementa integração com o provedor VoiceRSS e gerencia o fluxo de exceções.
 * * @author Thomaz Collet
 */
@Service
public class TextToSpeechService {

    private final RestTemplate restTemplate;
    private final String url;
    private final String apiKey;

    public TextToSpeechService(RestTemplate restTemplate, 
                               @Value("${voicerss.api.url}") String url,
                               @Value("${voicerss.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.apiKey = apiKey;
    }

    /**
     * Processa a conversão de texto para áudio via API externa.
     */
    public SpeechResponse speech(SpeechRequest request) {
        
        // 1. FILOSOFIA FAIL-FAST: Validação prematura para economia de recursos
        if (request.textToSpeak() != null && request.textToSpeak().length() > 1000) {
            throw new TextToSpeechLimitException();
        }

        try {
            // 2. CONSTRUÇÃO SEGURA DE URI: Uso do fromUriString para maior compatibilidade com a IDE
            // O Builder garante que espaços e acentos no texto sejam codificados corretamente para a URL
            String urlCompleta = UriComponentsBuilder.fromUriString(url)
                    .queryParam("key", apiKey)
                    .queryParam("src", request.textToSpeak())
                    .queryParam("hl", request.targetLanguage().getVoiceRssCode())
                    .queryParam("c", "MP3")
                    .queryParam("f", "22kHz_16bit_mono")
                    .queryParam("b64", "true")
                    .build()
                    .toUriString();

            // 3. COMUNICAÇÃO EXTERNA
            ResponseEntity<String> response = restTemplate.postForEntity(urlCompleta, null, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new SpeechServiceException("Serviço de áudio indisponível. Status: " + response.getStatusCode());
            }

            // 4. TRATAMENTO DE ERRO DE CONTEÚDO (Regra específica da VoiceRSS)
            if (response.getBody().startsWith("ERROR")) {
                throw new SpeechServiceException("Erro reportado pelo provedor de voz: " + response.getBody());
            }

            return new SpeechResponse(
                    response.getBody(),
                    request.textToSpeak(),
                    request.targetLanguage());

        } catch (SpeechServiceException | TextToSpeechLimitException e) {
            // REPASSA EXCEÇÕES DE NEGÓCIO: Crucial para os testes unitários passarem!
            throw e; 
        } catch (Exception e) {
            // ENVELOPAMENTO TÉCNICO: Captura erros de rede ou timeout
            throw new SpeechServiceException("Falha crítica na comunicação com o serviço de voz.", e);
        }
    }
}