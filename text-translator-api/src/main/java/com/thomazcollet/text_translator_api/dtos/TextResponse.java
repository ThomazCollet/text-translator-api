package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

/**
 * Data Transfer Object que encapsula o resultado completo de uma operação de tradução.
 * * @param sourceText     Texto original enviado pelo usuário.
 * @param translatedText Resultado da tradução processada.
 * @param sourceLanguage Idioma identificado ou definido na origem.
 * @param targetLanguage Idioma final da tradução.
 * @param audioBase64    Representação binária (Base64) do áudio para síntese de voz (opcional).
 */
public record TextResponse(
    String sourceText,
    String translatedText,
    Language sourceLanguage,
    Language targetLanguage,
    String audioBase64
) {}