package com.thomazcollet.text_translator_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.exception.TextToSpeechLimitException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Text-to-Speech")
class TextToSpeechServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private TextToSpeechService service;
    private SpeechRequest standardRequest;

    private static final String MOCK_URL = "http://api.voicerss.test";
    private static final String MOCK_KEY = "123456";

    @BeforeEach
    void setup() {
        service = new TextToSpeechService(restTemplate, MOCK_URL, MOCK_KEY);
        standardRequest = new SpeechRequest("Olá mundo", Language.PT_BR);
    }

    @Test
    @DisplayName("Deve retornar áudio em Base64 quando a API responder com sucesso")
    void shouldReturnSpeechResponseWhenApiCallIsSuccessful() {
        var fakeBase64 = "U29tZSBmYWtlIGF1ZGlvIGNvbnRlbnQ=";
        var fakeEntity = new ResponseEntity<>(fakeBase64, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(fakeEntity);

        var response = service.speech(standardRequest);

        assertNotNull(response.audioBase64());
        assertEquals("Olá mundo", response.spokenText());
        assertEquals(Language.PT_BR, response.targetLanguage());
    }

    @Test
    @DisplayName("Deve lançar TextToSpeechLimitException quando o texto exceder 1000 caracteres")
    void shouldThrowLimitExceptionWhenTextIsTooLong() {
        var longText = "a".repeat(1001);
        var longRequest = new SpeechRequest(longText, Language.EN);

        assertThrows(TextToSpeechLimitException.class, () -> service.speech(longRequest));
    }

    @Test
    @DisplayName("Deve lançar SpeechServiceException quando o provedor retornar uma String de erro")
    void shouldThrowSpeechServiceExceptionWhenApiReturnsErrorString() {
        var errorBody = "ERROR: The API key is invalid";
        var fakeEntity = new ResponseEntity<>(errorBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(fakeEntity);

        var ex = assertThrows(SpeechServiceException.class, () -> service.speech(standardRequest));
        
        // MENSAGEM ATUALIZADA:
        assertEquals("Erro do provedor VoiceRSS: ERROR: The API key is invalid", ex.getMessage());
    }

    @Test
    @DisplayName("Deve lançar SpeechServiceException quando o status HTTP for erro")
    void shouldThrowSpeechServiceExceptionWhenHttpStatusCodeIsError() {
        ResponseEntity<String> errorEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(errorEntity);

        assertThrows(SpeechServiceException.class, () -> service.speech(standardRequest));
    }

    @Test
    @DisplayName("Deve lançar SpeechServiceException em caso de falha técnica de rede")
    void shouldThrowSpeechServiceExceptionWhenNetworkFails() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection timed out"));

        var ex = assertThrows(SpeechServiceException.class, () -> service.speech(standardRequest));
        
        // MENSAGEM ATUALIZADA:
        assertEquals("Falha técnica ao gerar síntese de voz.", ex.getMessage());
    }
}