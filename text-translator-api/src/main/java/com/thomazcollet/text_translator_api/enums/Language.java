package com.thomazcollet.text_translator_api.enums;

// Representa os idiomas suportados pela aplicação
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

    public static Language fromLibreCode(String code) {
    for (Language lang : values()) {
        if (lang.libreCode.equalsIgnoreCase(code)) {
            return lang;
        }
    }
    return AUTO; // Fallback caso não encontre
    }

    public String getLibreCode() {
        return libreCode;
    }

    public String getVoiceRssCode() {
        return voiceRssCode;
    }
}

