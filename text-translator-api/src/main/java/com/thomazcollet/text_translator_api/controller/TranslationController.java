package com.thomazcollet.text_translator_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.service.TranslationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller responsável por expor os recursos de tradução da API.
 * Atua como ponto de entrada para as requisições, garantindo a validação dos
 * dados
 * antes do processamento.
 */
@RestController
@RequestMapping("/translate")
@Tag(name = "Translation", description = "Endpoints para processamento linguístico e tradução de textos")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Operation(summary = "Executa a tradução de um texto", description = "Processa a tradução entre idiomas suportados. Implementa estratégia de 'Bridge' (via EN) para idiomas periféricos e cache distribuído via Redis para otimização de performance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tradução processada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou texto vazio"),
            @ApiResponse(responseCode = "500", description = "Erro de conectividade com o motor de tradução externo") })

    /**
     * Processa solicitações de tradução de texto.
     * * @param request DTO contendo o texto e os idiomas (Source/Target).
     * 
     * @return ResponseEntity contendo os dados da tradução e status HTTP 200.
     */
    @PostMapping
    public ResponseEntity<TextResponse> translate(@RequestBody @Valid TextRequest request) {
        var response = translationService.translate(request);
        return ResponseEntity.ok(response);
    }
}