package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpeechRequest(
    @NotBlank(message = "O texto a ser falado não pode estar vazio")
    String textToSpeak,
    
    @NotNull(message = "O idioma de destino deve ser informado")
    Language targetLanguage
) {}