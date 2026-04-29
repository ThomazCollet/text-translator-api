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

import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.enums.Language;

/**
 * Teste de integração focado na camada de cache do serviço de tradução.
 * Garante que chamadas idênticas não sobrecarreguem a infraestrutura externa.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=simple")
@DisplayName("Integração: Cache de Tradução")
class TranslationServiceIntegrationTest {

    @Autowired
    private TranslationService translationService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Deve buscar do cache na segunda chamada para evitar nova requisição HTTP")
    void shouldRetrieveFromCacheOnSecondCall() {
        // Given
        var request = new TextRequest("Olá", Language.PT_BR, Language.EN);
        var mockLibreResponse = new LibreTranslateResponse("Hello", null);

        when(restTemplate.postForEntity(anyString(), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(ResponseEntity.ok(mockLibreResponse));

        // When
        translationService.translate(request); // Primeira chamada: Processamento normal
        translationService.translate(request); // Segunda chamada: Deve vir do cache

        // Then
        // Validamos que a API externa (simulada pelo RestTemplate) foi acionada exatamente UMA vez
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(LibreTranslateResponse.class));
    }
}