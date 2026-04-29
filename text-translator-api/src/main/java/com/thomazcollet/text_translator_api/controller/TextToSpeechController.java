package com.thomazcollet.text_translator_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.service.TextToSpeechService;

import jakarta.validation.Valid;

/**
 * Controller responsável pelos recursos de síntese de voz (TTS).
 * Expõe endpoints para conversão de texto em áudio processado.
 */
@RestController
@RequestMapping("/speech")
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    /**
     * Recebe um texto e retorna sua representação em áudio Base64.
     * * @param request DTO com o texto e idioma para síntese.
     * 
     * @return ResponseEntity contendo o áudio e metadados.
     */
    @PostMapping
    public ResponseEntity<SpeechResponse> speech(@RequestBody @Valid SpeechRequest request) {
        var response = textToSpeechService.speech(request);
        return ResponseEntity.ok(response);
    }
}