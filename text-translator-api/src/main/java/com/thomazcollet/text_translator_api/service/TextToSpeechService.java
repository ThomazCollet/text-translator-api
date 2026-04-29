package com.thomazcollet.text_translator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.exception.TextToSpeechLimitException;

/**
 * Serviço para conversão de texto em fala (TTS) utilizando VoiceRSS.
 * Gerencia a comunicação externa e o cache de áudios curtos.
 */
@Service
public class TextToSpeechService {

    private static final String CACHE_NAME = "audio_speech";
    private static final int CACHE_LIMIT = 150;
    private static final int TEXT_LIMIT = 1000;

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
     * Converte texto em áudio Base64. 
     * Áudios com menos de 150 caracteres são cacheados para otimizar performance.
     */
    @Cacheable(value = CACHE_NAME, 
               key = "{ #request.textToSpeak().toLowerCase().trim(), #request.targetLanguage() }", 
               condition = "#request.textToSpeak().length() < " + CACHE_LIMIT)
    public SpeechResponse speech(SpeechRequest request) {
        validateLimits(request);

        try {
            var urlCompleta = buildUri(request);
            var response = restTemplate.postForEntity(urlCompleta, null, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new SpeechServiceException("Serviço de voz indisponível. Status: " + response.getStatusCode());
            }

            if (response.getBody().startsWith("ERROR")) {
                throw new SpeechServiceException("Erro do provedor VoiceRSS: " + response.getBody());
            }

            return new SpeechResponse(
                    response.getBody(),
                    request.textToSpeak(),
                    request.targetLanguage());

        } catch (SpeechServiceException | TextToSpeechLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new SpeechServiceException("Falha técnica ao gerar síntese de voz.", e);
        }
    }

    private void validateLimits(SpeechRequest request) {
        if (request.textToSpeak() != null && request.textToSpeak().length() > TEXT_LIMIT) {
            throw new TextToSpeechLimitException();
        }
    }

    private String buildUri(SpeechRequest request) {
        return UriComponentsBuilder.fromUriString(url)
                .queryParam("key", apiKey)
                .queryParam("src", request.textToSpeak())
                .queryParam("hl", request.targetLanguage().getVoiceRssCode())
                .queryParam("c", "MP3")
                .queryParam("f", "22kHz_16bit_mono")
                .queryParam("b64", "true")
                .build()
                .toUriString();
    }
}