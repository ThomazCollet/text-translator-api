package com.thomazcollet.text_translator_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.enums.Language;

/**
 * Testes de integração para validação da política de cache do Text-to-Speech.
 * Verifica se a economia de recursos (memória) está sendo aplicada
 * corretamente.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=simple")
@DisplayName("Integração: Política de Cache de Voz")
class TextToSpeechServiceIT {

    @Autowired
    private TextToSpeechService ttsService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Deve buscar áudio do cache na segunda chamada para textos curtos")
    void shouldRetrieveAudioFromCacheForShortText() {
        // Given
        var request = new SpeechRequest("Olá, mundo", Language.PT_BR);
        var mockAudioBase64 = "U29tZUF1ZGlvRGF0YQ==";

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockAudioBase64));

        // When
        ttsService.speech(request); // Primeira chamada (Popula o cache)
        ttsService.speech(request); // Segunda chamada (Deve ler do cache)

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Não deve usar cache para textos longos (acima de 150 caracteres) para preservar memória")
    void shouldNotUseCacheForLongTexts() {
        // Given
        // Texto propositalmente longo para exceder o limite de 150 caracteres
        var longText = "Este é um texto propositalmente longo, com mais de cento e cinquenta caracteres, " +
                "criado especificamente para validar que a nossa condição de cache está ignorando " +
                "conteúdos muito grandes para poupar a memória do nosso servidor Redis no ambiente real.";

        var request = new SpeechRequest(longText, Language.PT_BR);
        var mockAudioBase64 = "TG9uZ0F1ZGlvRGF0YQ==";

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockAudioBase64));

        // When
        ttsService.speech(request); // Primeira chamada
        ttsService.speech(request); // Segunda chamada (Deve ignorar o cache)

        // Then
        // Verificamos que foram feitas duas chamadas reais ao RestTemplate
        verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(String.class));
    }
}