package com.thomazcollet.text_translator_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.exception.TextToSpeechLimitException;

/**
 * Serviço especializado na conversão de texto para fala (TTS).
 * Gerencia a integração com a API VoiceRSS e valida os limites operacionais.
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
     * Converte o texto fornecido em uma string Base64 representando o áudio MP3.
     * @param request DTO com o texto e o idioma alvo.
     * @return SpeechResponse contendo o Base64 e metadados.
     */
    public SpeechResponse speech(SpeechRequest request) {
        
        // 1. FAIL-FAST: Validação de limite de caracteres para evitar chamadas inúteis
        if (request.textToSpeak() != null && request.textToSpeak().length() > 1000) {
            throw new TextToSpeechLimitException();
        }

        try {
            // Construção da URL de integração
            String urlCompleta = String.format(
                    "%s?key=%s&src=%s&hl=%s&c=%s&f=%s&b64=%s",
                    url,
                    apiKey,
                    request.textToSpeak(),
                    request.targetLanguage().getVoiceRssCode(),
                    "MP3",
                    "22kHz_16bit_mono",
                    "true");

            // 2. Chamada à API externa
            ResponseEntity<String> response = restTemplate.postForEntity(urlCompleta, null, String.class);

            // Validação de status HTTP e corpo vazio
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new SpeechServiceException("A API de áudio retornou um status de erro: " + response.getStatusCode());
            }

            // A VoiceRSS retorna erros comuns (como chave inválida) dentro do corpo 200 OK começando com "ERROR"
            if (response.getBody().startsWith("ERROR")) {
                throw new SpeechServiceException("Erro reportado pelo provedor de voz: " + response.getBody());
            }

            return new SpeechResponse(
                    response.getBody(),
                    request.textToSpeak(),
                    request.targetLanguage());

        } catch (TextToSpeechLimitException e) {
            // Relança exceções de negócio sem encapsular em erro de infraestrutura
            throw e; 
        } catch (Exception e) {
            // Captura erros de rede, timeout ou parsing e envelopa na exceção de infraestrutura
            throw new SpeechServiceException("Falha crítica na comunicação com o serviço de voz.", e);
        }
    }
}