package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção lançada quando a aplicação recebe ou detecta um idioma que não faz 
 * parte do catálogo de idiomas suportados.
 * * Utilizada para garantir a consistência dos dados antes de processar 
 * chamadas de tradução.
 */

public class LanguageNotSupportedException extends RuntimeException {

    // Construtor Padrão
    public LanguageNotSupportedException() {
        super("O idioma fornecido ou detectado não é suportado pela aplicação. Atualmente, o sistema suporta apenas Português, Inglês e Espanhol.");
    }

    // Construtor Customizado 
    public LanguageNotSupportedException(String message) {
        super(message);
    }
}