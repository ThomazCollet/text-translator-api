package com.thomazcollet.text_translator_api.enums;

// Representa os idiomas suportados pela aplicação
public enum Language {
    EN("en"),
    PT_BR("pt"),
    ES("es");

    private final String libreCode;

    Language(String libreCode){
        this.libreCode = libreCode;
    }

    public String getLibreCode(){
        return libreCode;
    }
}
