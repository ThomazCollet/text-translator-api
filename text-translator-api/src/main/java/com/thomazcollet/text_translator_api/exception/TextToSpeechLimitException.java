package com.thomazcollet.text_translator_api.exception;

/**
 * Exceção de negócio lançada quando o volume de texto excede a capacidade operacional permitida.
 * * O uso desta exceção reflete a implementação do padrão 'Fail-Fast', evitando 
 * o consumo desnecessário de cotas de APIs externas e otimizando o tempo de resposta.
 */
public class TextToSpeechLimitException extends RuntimeException {

    /**
     * Construtor padrão com orientação de correção para o usuário.
     * Mapeado preferencialmente para o status HTTP 400 (Bad Request).
     */
    public TextToSpeechLimitException() {
        super("Não foi possível processar a solicitação de voz devido ao tamanho do conteúdo. Reduza o volume de caracteres e tente novamente.");
    }

    /**
     * Permite o detalhamento de limites específicos (ex: informar o máximo de caracteres permitido).
     * @param message Mensagem customizada sobre o limite excedido.
     */
    public TextToSpeechLimitException(String message) {
        super(message);
    }
}