package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

/**
 * DTO responsável por representar a resposta da tradução.
 * Contém o texto original, o texto traduzido, os idiomas envolvidos
 * e o áudio da pronúncia em formato Base64.
 */
public record TextResponse(
    String sourceText,
    String translatedText,
    Language sourceLanguage,
    Language targetLanguage,
    String audioBase64
) {}
