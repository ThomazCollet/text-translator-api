package com.thomazcollet.text_translator_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.exception.TextToSpeechLimitException;

@ExtendWith(MockitoExtension.class)
public class TextToSpeechServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private TextToSpeechService service;
    private SpeechRequest standardRequest;

    private static final String MOCK_URL = "http://api.voicerss.test";
    private static final String MOCK_KEY = "123456";

    @BeforeEach
    void setup() {
        // Injetamos os mocks e os valores fake de URL e KEY
        service = new TextToSpeechService(restTemplate, MOCK_URL, MOCK_KEY);

        // Request padrão para os testes
        standardRequest = new SpeechRequest("Olá mundo", Language.PT_BR);
    }

    @Test
    void shouldReturnSpeechResponseWhenApiCallIsSuccessful() {
        // --- ARRANGE ---
        String fakeBase64 = "U29tZSBmYWtlIGF1ZGlvIGNvbnRlbnQ="; // Representa um áudio fictício
        ResponseEntity<String> fakeEntity = new ResponseEntity<>(fakeBase64, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(fakeEntity);

        // --- ACT ---
        SpeechResponse response = service.speech(standardRequest);

        // --- ASSERT ---
        assertNotNull(response.audioBase64());
        assertEquals("Olá mundo", response.spokenText());
        assertEquals(Language.PT_BR, response.targetLanguage());
    }

    @Test
    void shouldThrowLimitExceptionWhenTextIsTooLong() {
        // --- ARRANGE ---
        // Criando um texto com 1001 caracteres para estourar o limite
        String longText = "a".repeat(1001);
        SpeechRequest longRequest = new SpeechRequest(longText, Language.EN);

        // --- ACT & ASSERT ---
        assertThrows(TextToSpeechLimitException.class, () -> {
            service.speech(longRequest);
        });
    }

    @Test
    void shouldThrowSpeechServiceExceptionWhenApiReturnsErrorString() {
        // --- ARRANGE ---
        // A VoiceRSS retorna 200 OK mas com a palavra ERROR no corpo em caso de falha
        // de chave
        String errorBody = "ERROR: The API key is invalid";
        ResponseEntity<String> fakeEntity = new ResponseEntity<>(errorBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(fakeEntity);

        // --- ACT & ASSERT ---
        SpeechServiceException ex = assertThrows(SpeechServiceException.class, () -> {
            service.speech(standardRequest);
        });

        // Verificamos se a mensagem de erro que você configurou no Service está lá
        assertEquals("Erro reportado pelo provedor de voz: ERROR: The API key is invalid", ex.getMessage());
    }

    @Test
    void shouldThrowSpeechServiceExceptionWhenHttpStatusCodeIsError() {
        // --- ARRANGE ---
        // Simulando um erro de infraestrutura (Ex: 404 ou 500)
        ResponseEntity<String> errorEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(errorEntity);

        // --- ACT & ASSERT ---
        assertThrows(SpeechServiceException.class, () -> {
            service.speech(standardRequest);
        });
    }

    @Test
    void shouldThrowSpeechServiceExceptionWhenNetworkFails() {
        // --- ARRANGE ---
        // Simulando uma queda de rede/timeout (lançando uma exceção genérica)
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection timed out"));

        // --- ACT & ASSERT ---
        SpeechServiceException ex = assertThrows(SpeechServiceException.class, () -> {
            service.speech(standardRequest);
        });

        assertEquals("Falha crítica na comunicação com o serviço de voz.", ex.getMessage());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Funcionalidade de configuração customizada de áudio ainda não implementada no Service")
    void shouldThrowInvalidAudioConfigExceptionWhenParamsAreIncompatible() {
        // --- ARRANGE ---
        // Simulando um cenário futuro onde o usuário poderia escolher um codec não
        // suportado
        SpeechRequest incompatibleRequest = new SpeechRequest("Teste", Language.PT_BR);

        // --- ACT & ASSERT ---
        // Por enquanto, como o Service não valida isso dinamicamente, o teste fica
        // desativado
        assertThrows(com.thomazcollet.text_translator_api.exception.InvalidAudioConfigException.class, () -> {
            service.speech(incompatibleRequest);
        });
    }
}