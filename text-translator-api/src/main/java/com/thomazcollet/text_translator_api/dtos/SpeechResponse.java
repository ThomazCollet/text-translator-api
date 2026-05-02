package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object que encapsula o resultado de uma síntese de voz.
 * * @param audioBase64 Representação do áudio em formato Base64 (pronto para
 * reprodução no browser).
 * 
 * @param spokenText     O texto que foi processado e narrado.
 * @param targetLanguage O idioma e sotaque utilizados na geração do áudio.
 */
public record SpeechResponse(
    @Schema(description = "Representação binária do áudio em Base64 (formato MP3)")
    String audioBase64,
    
    @Schema(description = "Texto que foi processado para narração", example = "Bem-vindo ao BridgeTranslate...")
    String spokenText,
    
    @Schema(description = "Idioma utilizado na síntese")
    Language targetLanguage
) {}