package com.thomazcollet.text_translator_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.thomazcollet.text_translator_api.dtos.DetectedLanguageDetails;
import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.exception.ExternalApiException;
import com.thomazcollet.text_translator_api.exception.InvalidTextException;
import com.thomazcollet.text_translator_api.exception.LanguageNotSupportedException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Tradução")
class TranslationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private TranslationService service;
    private TextRequest standardRequest;

    private static final String MOCK_URL = "http://api.translate.test";

    @BeforeEach
    void setup() {
        service = new TranslationService(restTemplate, MOCK_URL);
        standardRequest = new TextRequest("Hello", Language.EN, Language.PT_BR);
    }

    @Test
    @DisplayName("Deve retornar o texto traduzido quando a API responder com sucesso")
    void shouldReturnTranslatedTextWhenApiCallIsSuccessful() {
        // Given
        var fakeBody = new LibreTranslateResponse("Olá", null);
        var fakeEntity = new ResponseEntity<>(fakeBody, HttpStatus.OK);

        when(restTemplate.postForEntity(eq(MOCK_URL), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(fakeEntity);

        // When
        var response = service.translate(standardRequest);

        // Then
        assertEquals("Olá", response.translatedText());
        assertEquals("Hello", response.sourceText());
    }

    @Test
    @DisplayName("Deve lançar exceção se o texto de entrada estiver em branco")
    void shouldThrowInvalidTextExceptionWhenTextIsBlank() {
        // Given
        var invalidRequest = new TextRequest("", Language.PT_BR, Language.EN);

        // When / Then
        assertThrows(InvalidTextException.class, () -> service.translate(invalidRequest));
    }

    @Test
    @DisplayName("Deve usar tradução em ponte (EN) ao traduzir entre idiomas periféricos")
    void shouldUseBridgeWhenTranslatingBetweenNonEnglishLanguages() {
        // Given
        var bridgeRequest = new TextRequest("Bom dia", Language.PT_BR, Language.ES);
        var firstStepBody = new LibreTranslateResponse("Good morning", null);
        var secondStepBody = new LibreTranslateResponse("Buenos días", null);

        when(restTemplate.postForEntity(eq(MOCK_URL), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(new ResponseEntity<>(firstStepBody, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(secondStepBody, HttpStatus.OK));

        // When
        var response = service.translate(bridgeRequest);

        // Then
        assertEquals("Buenos días", response.translatedText());
        assertEquals(Language.ES, response.targetLanguage());
    }

    @Test
    @DisplayName("Deve identificar corretamente o idioma de origem quando o modo AUTO é usado")
    void shouldDetectLanguageCorrectyWhenSourceIsAuto() {
        // Given
        var autoRequest = new TextRequest("Hello", Language.AUTO, Language.PT_BR);
        var detected = new DetectedLanguageDetails("en", 100.0);
        var fakeBody = new LibreTranslateResponse("Olá", detected);

        when(restTemplate.postForEntity(any(String.class), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(new ResponseEntity<>(fakeBody, HttpStatus.OK));

        // When
        var response = service.translate(autoRequest);

        // Then
        assertEquals(Language.EN, response.sourceLanguage());
    }

    @Test
    @DisplayName("Deve lançar ExternalApiException se a API remota retornar erro HTTP")
    void shouldThrowExternalApiExceptionWhenApiReturnsError() {
        // Given
        ResponseEntity<LibreTranslateResponse> errorEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(any(String.class), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(errorEntity);

        // When / Then
        assertThrows(ExternalApiException.class, () -> service.translate(standardRequest));
    }

    @Test
    @DisplayName("Deve lançar LanguageNotSupportedException se o idioma detectado for inválido")
    void shouldThrowLanguageNotSupportedExceptionWhenApiDetectsUnsupportedLanguage() {
        // Given
        var autoRequest = new TextRequest("Bonjour", Language.AUTO, Language.PT_BR);
        var detected = new DetectedLanguageDetails("fr", 100.0);
        var fakeBody = new LibreTranslateResponse("Bom dia", detected);

        when(restTemplate.postForEntity(any(String.class), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(new ResponseEntity<>(fakeBody, HttpStatus.OK));

        // When / Then
        assertThrows(LanguageNotSupportedException.class, () -> service.translate(autoRequest));
    }
}