package com.thomazcollet.text_translator_api.dtos;

/**
 * Detalhes sobre a detecção automática de idioma realizada pela API externa.
 * * @param language Código do idioma identificado (ex: "en", "pt").
 * 
 * @param confidence Nível de confiança da detecção (0.0 a 1.0).
 */
public record DetectedLanguageDetails(
        String language,
        Double confidence) {
}
