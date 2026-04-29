package com.thomazcollet.text_translator_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.enums.Language;
import com.thomazcollet.text_translator_api.infra.handler.GlobalExceptionHandler;
import com.thomazcollet.text_translator_api.service.TranslationService;

/**
 * Testes de integração da camada Web do tradutor.
 * Valida o mapeamento de endpoints, serialização JSON e tratamento de exceções.
 */
@WebMvcTest(controllers = TranslationController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Testes do Controller de Tradução")
class TranslationControllerTest {

    @MockitoBean
    private TranslationService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 200 e a tradução correta quando os dados são válidos")
    void shouldReturnOkWhenTranslationIsSuccessful() throws Exception {
        var request = new TextRequest("Hello", Language.EN, Language.PT_BR);
        var response = new TextResponse("Hello", "Olá", Language.EN, Language.PT_BR, null);
        
        when(service.translate(any(TextRequest.class))).thenReturn(response);

        mockMvc.perform(post("/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translatedText").value("Olá"))
                .andExpect(jsonPath("$.sourceLanguage").value("EN"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando a validação do Bean Validation falhar (texto vazio)")
    void shouldReturnBadRequestWhenBeanValidationFails() throws Exception {
        // Texto vazio aciona o @NotBlank no TextRequest
        var request = new TextRequest("", Language.EN, Language.PT_BR);

        mockMvc.perform(post("/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists()); 
                // Aqui o Spring valida ANTES de chamar o service.
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorrer uma falha inesperada no sistema")
    void shouldReturnInternalServerErrorOnSystemFailure() throws Exception {
        var request = new TextRequest("Hello", Language.EN, Language.PT_BR);

        doThrow(new RuntimeException("Falha catastrófica")).when(service).translate(any());

        mockMvc.perform(post("/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde."));
    }
}