package com.thomazcollet.text_translator_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.service.TextToSpeechService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller responsável pelos recursos de síntese de voz (TTS).
 * Expõe endpoints para conversão de texto em áudio processado.
 */
@RestController
@RequestMapping("/speech")
@Tag(name = "2. Speech", description = "Recursos de síntese de voz (Text-to-Speech) utilizando VoiceRSS")
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    @Operation(summary = "Converter texto em áudio", description = "Recebe um texto e o converte em uma string Base64 (MP3). "
            +
            "Textos com menos de 150 caracteres são automaticamente cacheados no Redis para máxima performance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Áudio gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Texto inválido ou acima de 1000 caracteres"),
            @ApiResponse(responseCode = "422", description = "Erro de limite ou processamento no provedor de voz"),
            @ApiResponse(responseCode = "500", description = "Falha técnica na síntese de voz")
    })

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