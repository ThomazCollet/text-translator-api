package com.thomazcollet.text_translator_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.infra.handler.GlobalExceptionHandler;
import com.thomazcollet.text_translator_api.service.TextToSpeechService;

/**
 * Testes de unidade para o TextToSpeechController.
 * Valida a exposição do endpoint de áudio e o tratamento de erros de integração.
 */
@WebMvcTest(controllers = TextToSpeechController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Testes do Controller de Voz")
class TextToSpeechControllerTest {

    @MockitoBean
    private TextToSpeechService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 200 e o áudio em Base64 quando a conversão for bem-sucedida")
    void shouldReturnOkWhenSpeechConversionIsSuccessful() throws Exception {
        var request = new SpeechRequest("Texto para voz", Language.PT_BR);
        var response = new SpeechResponse("U0m3Base64String...", "Texto para voz", Language.PT_BR);

        when(service.speech(any(SpeechRequest.class))).thenReturn(response);

        mockMvc.perform(post("/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audioBase64").value("U0m3Base64String..."))
                .andExpect(jsonPath("$.spokenText").value("Texto para voz"))
                .andExpect(jsonPath("$.targetLanguage").value("PT_BR"));
    }

    @Test
    @DisplayName("Deve retornar 502 Bad Gateway quando o serviço externo de voz reportar erro")
    void shouldReturnBadGatewayWhenVoiceProviderFails() throws Exception {
        var request = new SpeechRequest("Hello", Language.EN);
        var errorMessage = "Erro no provedor VoiceRSS";

        when(service.speech(any(SpeechRequest.class)))
                .thenThrow(new SpeechServiceException(errorMessage));

        mockMvc.perform(post("/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.error").value("Bad Gateway"));
    }
}