package com.thomazcollet.text_translator_api.dtos;

import com.thomazcollet.text_translator_api.enums.Language;

public record SpeechResponse(
    String audioBase64,
    String spokenText,      // O texto que foi narrado
    Language targetLanguage // O idioma da narração
) {}