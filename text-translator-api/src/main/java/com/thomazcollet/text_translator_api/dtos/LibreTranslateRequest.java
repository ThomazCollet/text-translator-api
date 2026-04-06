package com.thomazcollet.text_translator_api.dtos;

public record LibreTranslateRequest(
    String q,
    String source,
    String target) {}
