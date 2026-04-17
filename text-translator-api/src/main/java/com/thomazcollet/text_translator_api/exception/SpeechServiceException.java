package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção de infraestrutura lançada para falhas na integração com o serviço de síntese de voz (TTS).
 */
public class SpeechServiceException extends RuntimeException {

    /**
     * Construtor padrão com mensagem amigável para o cliente final.
     * Ideal para garantir que detalhes técnicos sensíveis não sejam expostos na API.
     */
    public SpeechServiceException() {
        super("O serviço de conversão de texto em fala encontrou uma instabilidade ao processar sua solicitação.");
    }

    /**
     * Permite o lançamento da exceção com uma mensagem específica de contexto.
     * @param message Mensagem detalhando o erro.
     */
    public SpeechServiceException(String message) {
        super(message);
    }

    /**
     * Construtor que preserva a pilha de erro (Stack Trace) original.
     * Crucial para observabilidade e depuração em sistemas distribuídos.
     * @param message Contexto do erro para a resposta da API.
     * @param cause A exceção raiz que originou a falha (ex: erro de conexão).
     */
    public SpeechServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}