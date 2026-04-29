package com.thomazcollet.text_translator_api.dtos;

/**
 * DTO interno para comunicação com a API do LibreTranslate.
 * * @param q      O texto a ser traduzido (Query).
 * @param source O código do idioma de origem.
 * @param target O código do idioma de destino.
 */
public record LibreTranslateRequest(
    String q,
    String source,
    String target
) {}