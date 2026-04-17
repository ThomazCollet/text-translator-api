package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção lançada quando o conteúdo textual enviado para tradução não atende
 * aos requisitos mínimos de integridade (ex: texto vazio ou nulo).
 */

public class InvalidTextException extends RuntimeException{
    public InvalidTextException (){
        super("O texto fornecido para tradução é inválido ou está vazio.");
    }

    public InvalidTextException (String message){
        super(message);
    }
}
