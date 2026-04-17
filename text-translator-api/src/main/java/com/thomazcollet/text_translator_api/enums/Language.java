package com.thomazcollet.text_translator_api.enums;

import com.thomazcollet.text_translator_api.exception.LanguageNotSupportedException;

/**
 * Define os idiomas suportados pela aplicação e seus respectivos códigos
 * para integração com APIs de tradução e síntese de voz.
 */
public enum Language {
    AUTO("auto", "auto"),
    EN("en", "en-us"),
    PT_BR("pt", "pt-br"),
    ES("es", "es-es");

    private final String libreCode;
    private final String voiceRssCode;

    Language(String libreCode, String voiceRssCode) {
        this.libreCode = libreCode;
        this.voiceRssCode = voiceRssCode;
    }

    /**
     * Mapeia um código de idioma da API externa para o Enum correspondente.
     * @param code Código retornado pela API (ex: "pt", "en")
     * @return O Enum Language correspondente
     * @throws LanguageNotSupportedException se o código não for encontrado no catálogo.
     */
    public static Language fromLibreCode(String code) {
        for (Language lang : values()) {
            if (lang.libreCode.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        throw new LanguageNotSupportedException();
    }

    public String getLibreCode() {
        return libreCode;
    }

    public String getVoiceRssCode() {
        return voiceRssCode;
    }
}