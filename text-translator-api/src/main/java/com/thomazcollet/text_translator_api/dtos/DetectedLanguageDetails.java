package com.thomazcollet.text_translator_api.dtos;

public record DetectedLanguageDetails(
    String language,
    Double confidence
) {}
