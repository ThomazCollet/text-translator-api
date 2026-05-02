package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object para requisições de tradução.
 * Utiliza Java Records para garantir imutabilidade e concisão.
 */
public record TextRequest(
    @Schema(description = "Texto original a ser traduzido", example = "Olá, como você está?") 
    @NotBlank(message = "O texto para tradução não pode estar vazio.") 
    @Size(max = 5000) String text,

    @Schema(description = "Idioma de origem. Use 'AUTO' para detecção automática.", example = "PT") 
    @NotNull(message = "O idioma de origem deve ser informado.") Language sourceLanguage,

    @Schema(description = "Idioma de destino para a tradução.", example = "EN") 
    @NotNull(message = "O idioma de destino deve ser informado.") Language targetLanguage
) {}