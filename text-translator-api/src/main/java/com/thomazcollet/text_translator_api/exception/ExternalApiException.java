package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção lançada quando ocorre uma falha na comunicação com serviços de tradução externos.
 * * Esta classe encapsula erros de conectividade, indisponibilidade da API ou retornos
 * de erro (4xx e 5xx) provenientes de fornecedores terceiros.
 */

public class ExternalApiException extends RuntimeException {
    
    public ExternalApiException (){
        super("Falha na comunicação com o serviço externo de tradução.");
    }

    public ExternalApiException (String message){
        super(message);
    }

    public ExternalApiException (String message, Throwable cause){
        super(message, cause);
    }
}
