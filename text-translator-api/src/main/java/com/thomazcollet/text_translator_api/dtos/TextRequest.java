package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object para requisições de tradução.
 * Utiliza Java Records para garantir imutabilidade e concisão.
 */
public record TextRequest(
    @NotBlank(message = "O texto para tradução não pode estar vazio.")
    @Size(max = 5000, message = "O texto excede o limite permitido de 5000 caracteres.")
    String text,

    @NotNull(message = "O idioma de origem deve ser informado.")
    Language sourceLanguage,

    @NotNull(message = "O idioma de destino deve ser informado.")
    Language targetLanguage
) {}