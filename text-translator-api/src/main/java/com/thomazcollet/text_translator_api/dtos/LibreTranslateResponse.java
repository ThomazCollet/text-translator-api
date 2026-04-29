package com.thomazcollet.text_translator_api.dtos;

/**
 * Representa a resposta estruturada retornada pela API do LibreTranslate.
 * * @param translatedText O resultado da tradução.
 * @param detectedLanguage Detalhes sobre o idioma identificado (quando enviado AUTO).
 */
public record LibreTranslateResponse(
    String translatedText,
    DetectedLanguageDetails detectedLanguage
) {}