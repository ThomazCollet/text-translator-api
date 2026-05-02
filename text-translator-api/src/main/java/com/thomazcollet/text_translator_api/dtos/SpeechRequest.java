package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO que representa uma solicitação de síntese de voz (Text-to-Speech).
 * * @param textToSpeak O conteúdo textual que será convertido em áudio.
 * 
 * @param targetLanguage O idioma e sotaque a serem aplicados na locução.
 */
public record SpeechRequest(
        @Schema(description = "Conteúdo textual para conversão em áudio", example = "Bem-vindo ao BridgeTranslate, sua ponte entre idiomas.") @NotBlank(message = "O texto a ser falado não pode estar vazio.") @Size(max = 1000) String textToSpeak,

        @Schema(description = "Idioma e sotaque para a locução", example = "PT_BR") @NotNull(message = "O idioma de destino deve ser informado.") Language targetLanguage) {
}