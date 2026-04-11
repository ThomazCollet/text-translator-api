package com.thomazcollet.text_translator_api.dtos;

public record LibreTranslateResponse(
    String translatedText,
    DetectedLanguageDetails detectedLanguage
) {}