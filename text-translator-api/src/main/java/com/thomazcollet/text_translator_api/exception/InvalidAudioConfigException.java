package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção lançada quando os parâmetros técnicos de áudio (como formato, codec ou taxa de amostragem)
 * não são compatíveis com as capacidades do provedor de síntese de voz (TTS).
 * * Esta classe demonstra um controle granular sobre as especificações técnicas da mídia processada.
 */
public class InvalidAudioConfigException extends RuntimeException {

    /**
     * Construtor padrão com mensagem informativa sobre a incompatibilidade de configuração.
     */
    public InvalidAudioConfigException() {
        super("As configurações de áudio solicitadas não são suportadas ou são inválidas para o provedor atual.");
    }

    /**
     * Permite detalhar qual parâmetro de configuração específico causou a falha.
     * @param message Detalhamento da configuração inválida.
     */
    public InvalidAudioConfigException(String message) {
        super(message);
    }

    /**
     * Permite preservar a causa original caso a falha ocorra durante uma 
     * conversão de tipos ou processamento de biblioteca de terceiros.
     * @param message Contexto do erro.
     * @param cause Exceção original que motivou a falha técnica.
     */
    public InvalidAudioConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}