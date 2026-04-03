package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO responsável por representar a requisição de tradução.
 * Contém o texto de entrada, o idioma de origem e o idioma de destino.
 */
public record TextRequest(
    @NotBlank String text,
    @NotNull Language sourceLanguage,
    @NotNull Language targetLanguage
) {}