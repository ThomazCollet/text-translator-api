package com.thomazcollet.text_translator_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.DetectedLanguageDetails;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.exception.ExternalApiException;
import com.thomazcollet.text_translator_api.exception.InvalidTextException;
import com.thomazcollet.text_translator_api.exception.LanguageNotSupportedException;

@ExtendWith(MockitoExtension.class)
public class TranslationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private TranslationService service;
    private TextRequest standardRequest; // Atributo da classe para ser usado em todos os testes

    private static final String MOCK_URL = "http://api.translate.test";

    @BeforeEach
    void setup() {
        service = new TranslationService(restTemplate, MOCK_URL);

        // Criamos um request padrão que será reutilizado
        standardRequest = new TextRequest("Hello", Language.EN, Language.PT_BR);
    }

    @Test
    void shouldReturnTranslatedTextWhenApiCallIsSuccessful() {
        // --- ARRANGE ---
        // Agora usando o DTO real que o service espera
        LibreTranslateResponse fakeBody = new LibreTranslateResponse("Olá", null);
        ResponseEntity<LibreTranslateResponse> fakeEntity = new ResponseEntity<>(fakeBody, HttpStatus.OK);

        when(restTemplate.postForEntity(eq(MOCK_URL), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(fakeEntity);

        // --- ACT ---
        TextResponse response = service.translate(standardRequest);

        // --- ASSERT ---
        assertEquals("Olá", response.translatedText());
        assertEquals("Hello", response.sourceText());
    }

    @Test
    void shouldThrowInvalidTextExceptionWhenTextIsBlank() {
        // ARRANGE
        // Criamos um request inválido especificamente para este teste
        TextRequest invalidRequest = new TextRequest("", Language.PT_BR, Language.EN);

        // ACT & ASSERT (Em testes de exceção, eles acontecem na mesma linha)
        assertThrows(InvalidTextException.class, () -> {
            service.translate(invalidRequest);
        });
    }

    @Test
    void shouldUseBridgeWhenTranslatingBetweenNonEnglishLanguages() {
        // --- ARRANGE ---
        // Request que exige ponte: Português -> Espanhol (ambos não são EN)
        TextRequest bridgeRequest = new TextRequest("Bom dia", Language.PT_BR, Language.ES);

        // Simulamos as duas etapas do processo no LibreTranslateResponse
        // 1ª etapa: PT_BR -> EN ("Good morning")
        LibreTranslateResponse firstStepBody = new LibreTranslateResponse("Good morning", null);
        // 2ª etapa: EN -> ES ("Buenos días")
        LibreTranslateResponse secondStepBody = new LibreTranslateResponse("Buenos días", null);

        ResponseEntity<LibreTranslateResponse> firstEntity = new ResponseEntity<>(firstStepBody, HttpStatus.OK);
        ResponseEntity<LibreTranslateResponse> secondEntity = new ResponseEntity<>(secondStepBody, HttpStatus.OK);

        // Ensinamos o Mockito a retornar as respostas em sequência
        when(restTemplate.postForEntity(eq(MOCK_URL), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(firstEntity)
                .thenReturn(secondEntity);

        // --- ACT ---
        TextResponse response = service.translate(bridgeRequest);

        // --- ASSERT ---
        // O resultado final deve ser o da segunda tradução (Espanhol)
        assertEquals("Buenos días", response.translatedText());
        // O texto de origem deve ser o original enviado pelo usuário
        assertEquals("Bom dia", response.sourceText());
        // Validamos se o idioma alvo no objeto de resposta é o Espanhol
        assertEquals(Language.ES, response.targetLanguage());
    }

    @Test
    void shouldDetectLanguageCorrectyWhenSourceIsAuto() {
        // --- ARRANGE ---
        TextRequest autoRequest = new TextRequest("Hello", Language.AUTO, Language.PT_BR);

        // CHAMADA DIRETA: Sem o prefixo do outro record
        // Use 100.0 (Double) em vez de 100 (Integer)
        DetectedLanguageDetails detected = new DetectedLanguageDetails("en", 100.0);

        LibreTranslateResponse fakeBody = new LibreTranslateResponse("Olá", detected);

        when(restTemplate.postForEntity(any(String.class), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(new ResponseEntity<>(fakeBody, HttpStatus.OK));

        // --- ACT ---
        TextResponse response = service.translate(autoRequest);

        // --- ASSERT ---
        assertEquals(Language.EN, response.sourceLanguage());
    }

    @Test
    void shouldThrowExternalApiExceptionWhenApiReturnsError() {
        // --- ARRANGE ---
        // Simulando um erro 500 (Server Error) na API externa
        ResponseEntity<LibreTranslateResponse> errorEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(any(String.class), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(errorEntity);

        // --- ACT & ASSERT ---
        assertThrows(ExternalApiException.class, () -> {
            service.translate(standardRequest);
        });
    }

    @Test
    void shouldThrowLanguageNotSupportedExceptionWhenApiDetectsUnsupportedLanguage() {
        // ARRANGE: API detecta "fr" (Francês), que não temos no nosso Enum
        TextRequest autoRequest = new TextRequest("Bonjour", Language.AUTO, Language.PT_BR);

        // Corrigido: Nome correto do DTO, sem prefixo e usando Double (100.0)
        DetectedLanguageDetails detected = new DetectedLanguageDetails("fr", 100.0);

        LibreTranslateResponse fakeBody = new LibreTranslateResponse("Bom dia", detected);

        when(restTemplate.postForEntity(any(String.class), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(new ResponseEntity<>(fakeBody, HttpStatus.OK));

        // ACT & ASSERT
        // O Service chamará Language.fromLibreCode("fr"), que deve lançar a exceção
        assertThrows(LanguageNotSupportedException.class, () -> {
            service.translate(autoRequest);
        });
    }
}