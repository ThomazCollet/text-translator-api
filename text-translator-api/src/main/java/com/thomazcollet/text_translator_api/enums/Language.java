package com.thomazcollet.text_translator_api.enums;

// Representa os idiomas suportados pela aplicação
public enum Language {
   EN("en"),
   PTBR("pt-BR"),
   ES("es");

   private final String code;

    Language(String code){
    this.code = code;
   }

   public String getCode(){
    return code;
   }
}
