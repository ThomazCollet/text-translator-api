package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO que representa uma solicitação de síntese de voz (Text-to-Speech).
 * * @param textToSpeak    O conteúdo textual que será convertido em áudio.
 * @param targetLanguage O idioma e sotaque a serem aplicados na locução.
 */
public record SpeechRequest(
    @NotBlank(message = "O texto a ser falado não pode estar vazio.")
    @Size(max = 1000, message = "O texto para áudio excede o limite permitido de 1000 caracteres.")
    String textToSpeak,
    
    @NotNull(message = "O idioma de destino deve ser informado.")
    Language targetLanguage
) {}