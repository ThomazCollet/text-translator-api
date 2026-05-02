package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object que encapsula o resultado completo de uma operação de tradução.
 * * @param sourceText     Texto original enviado pelo usuário.
 * @param translatedText Resultado da tradução processada.
 * @param sourceLanguage Idioma identificado ou definido na origem.
 * @param targetLanguage Idioma final da tradução.
 * @param audioBase64    Representação binária (Base64) do áudio para síntese de voz (opcional).
 */
public record TextResponse(
    @Schema(example = "Olá, como você está?") String sourceText,
    @Schema(example = "Hello, how are you?") String translatedText,
    @Schema(description = "Idioma identificado ou definido") Language sourceLanguage,
    @Schema(description = "Idioma final solicitado") Language targetLanguage,
    @Schema(description = "Áudio em Base64 (retornado apenas no endpoint de fala)") String audioBase64
) {}