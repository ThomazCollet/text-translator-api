package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção lançada quando ocorre uma falha específica durante o processo de
 * tradução em ponte
 * (ex: tradução intermediária para o inglês).
 */
public class TranslationBridgeException extends RuntimeException {

    public TranslationBridgeException() {
        super("Falha ao processar a tradução através da ponte de idiomas.");
    }

    public TranslationBridgeException(String message) {
        super(message);
    }

    public TranslationBridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
