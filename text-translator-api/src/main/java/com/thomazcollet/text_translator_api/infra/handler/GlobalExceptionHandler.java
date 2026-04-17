package com.thomazcollet.text_translator_api.infra.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.thomazcollet.text_translator_api.exception.ExternalApiException;
import com.thomazcollet.text_translator_api.exception.InvalidTextException;
import com.thomazcollet.text_translator_api.exception.LanguageNotSupportedException;
import com.thomazcollet.text_translator_api.exception.RestErrorMessage;
import com.thomazcollet.text_translator_api.exception.TranslationBridgeException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Centraliza a criação da resposta de erro padronizada.
     */
    private ResponseEntity<RestErrorMessage> buildResponseEntity(HttpStatus status, String message,
            HttpServletRequest request) {
        RestErrorMessage errorDetails = new RestErrorMessage(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(errorDetails);
    }

    // 1. SEGURANÇA: Erros inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.",
                request);
    }

    // 2. REGRAS DE NEGÓCIO: Texto Inválido (400)
    @ExceptionHandler(InvalidTextException.class)
    public ResponseEntity<RestErrorMessage> handleInvalidText(InvalidTextException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 3. REGRAS DE NEGÓCIO: Idioma não suportado (400 ou 422)
    @ExceptionHandler(LanguageNotSupportedException.class)
    public ResponseEntity<RestErrorMessage> handleLanguageNotSupported(LanguageNotSupportedException ex, HttpServletRequest request) {
        // Usamos BAD_REQUEST pois a entrada do usuário não condiz com o suporte da API
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 4. INFRAESTRUTURA: Erro na API Externa (502)
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<RestErrorMessage> handleExternalApi(ExternalApiException ex, HttpServletRequest request) {
        // BAD_GATEWAY indica que um servidor (o seu) recebeu uma resposta inválida de outro (LibreTranslate)
        return buildResponseEntity(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    // 5. PROCESSO: Falha na Tradução em Ponte (500)
    @ExceptionHandler(TranslationBridgeException.class)
    public ResponseEntity<RestErrorMessage> handleTranslationBridge(TranslationBridgeException ex, HttpServletRequest request) {
        // Como é uma falha no processo interno de orquestração das chamadas
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}