package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

/**
 * DTO responsável por representar a requisição de tradução.
 * Contém o texto de entrada, o idioma de origem e o idioma de destino.
 */
public record TextRequest(
    String text,
    Language sourceLanguage,
    Language targetLanguage
) {}