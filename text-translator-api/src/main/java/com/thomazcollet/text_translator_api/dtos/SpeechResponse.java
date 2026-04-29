package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

/**
 * Data Transfer Object que encapsula o resultado de uma síntese de voz.
 * * @param audioBase64 Representação do áudio em formato Base64 (pronto para
 * reprodução no browser).
 * 
 * @param spokenText     O texto que foi processado e narrado.
 * @param targetLanguage O idioma e sotaque utilizados na geração do áudio.
 */
public record SpeechResponse(
        String audioBase64,
        String spokenText,
        Language targetLanguage) {
}