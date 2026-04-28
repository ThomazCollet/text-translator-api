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

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cache.type=simple" // Desativa o Redis e usa cache em memória para o teste
})
class TextToSpeechServiceIT {

    @Autowired
    private TextToSpeechService ttsService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Deve buscar áudio do cache na segunda chamada para textos curtos")
    void shouldRetrieveAudioFromCacheForShortText() {
        // GIVEN
        var request = new SpeechRequest("Olá, mundo", Language.PT_BR);
        var mockAudioBase64 = "U29tZUF1ZGlvRGF0YQ=="; // Simulação de um Base64

        // A VoiceRSS retorna uma String diretamente no corpo da resposta
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockAudioBase64));

        // WHEN
        ttsService.speech(request); // 1ª chamada: Deve bater no "servidor externo" (mock)
        ttsService.speech(request); // 2ª chamada: Deve vir do cache interno

        // THEN
        // Provamos que o cache funcionou porque o RestTemplate só foi acionado 1 vez
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Não deve usar cache para textos longos (acima de 150 caracteres)")
    void shouldNotUseCacheForLongTexts() {
        // GIVEN
        String longText = "Este é um texto propositalmente longo, com mais de cento e cinquenta caracteres, " +
                          "criado especificamente para validar que a nossa condição de cache está ignorando " +
                          "conteúdos muito grandes para poupar a memória do nosso servidor Redis no ambiente real.";
        
        var request = new SpeechRequest(longText, Language.PT_BR);
        var mockAudioBase64 = "TG9uZ0F1ZGlvRGF0YQ==";

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockAudioBase64));

        // WHEN
        ttsService.speech(request); // 1ª chamada
        ttsService.speech(request); // 2ª chamada: Como o texto > 150, não deve estar no cache

        // THEN
        // O RestTemplate deve ter sido chamado 2 vezes, ignorando o cache
        verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(String.class));
    }
}