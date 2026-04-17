package com.thomazcollet.text_translator_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.service.TranslationService;

import jakarta.validation.Valid;

/**
 * Controller responsável por expor os endpoints de tradução.
 */
@RestController
@RequestMapping("/translate")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Endpoint para tradução de textos entre idiomas suportados.
     * @param request Objeto contendo o texto, idioma de origem e destino.
     * @return ResponseEntity contendo o corpo da tradução e status 200 OK.
     */
    @PostMapping
    public ResponseEntity<TextResponse> translate(@RequestBody @Valid TextRequest request) {
        final var response = translationService.translate(request);
        return ResponseEntity.ok(response);
    }
}