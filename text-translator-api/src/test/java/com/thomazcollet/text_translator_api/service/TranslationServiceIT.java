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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.thomazcollet.text_translator_api.dtos.LibreTranslateResponse;
import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.enums.Language;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cache.type=simple" // Isso agora vai desativar o CacheConfig automaticamente!
})
class TranslationServiceIntegrationTest {

    @Autowired
    private TranslationService translationService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Deve buscar do cache na segunda chamada com os mesmos parâmetros")
    void shouldRetrieveFromCacheOnSecondCall() {
        // GIVEN
        var request = new TextRequest("Olá", Language.PT_BR, Language.EN);
        var mockLibreResponse = new LibreTranslateResponse("Hello", null);

        when(restTemplate.postForEntity(anyString(), any(), eq(LibreTranslateResponse.class)))
                .thenReturn(ResponseEntity.ok(mockLibreResponse));

        // WHEN
        translationService.translate(request); // 1ª chamada (vai pro mock)
        translationService.translate(request); // 2ª chamada (deve ir pro cache)

        // THEN
        // Se o cache funcionar, o restTemplate só foi acionado 1 vez
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(LibreTranslateResponse.class));
    }
}