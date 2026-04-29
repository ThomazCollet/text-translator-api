package com.thomazcollet.text_translator_api.infra.handler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.thomazcollet.text_translator_api.exception.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Centralizador de exceções da API.
 * Converte falhas internas e erros de negócio em respostas HTTP padronizadas
 * (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura erros de validação do Bean Validation (@Valid).
     * Extrai as mensagens configuradas nos DTOs para retornar ao cliente.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorMessage> handleValidationErrors(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de validação: " + errors, request);
    }

    @ExceptionHandler({
            InvalidTextException.class,
            LanguageNotSupportedException.class,
            TextToSpeechLimitException.class,
            InvalidAudioConfigException.class
    })
    public ResponseEntity<RestErrorMessage> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({
            ExternalApiException.class,
            SpeechServiceException.class
    })
    public ResponseEntity<RestErrorMessage> handleBadGateway(RuntimeException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(TranslationBridgeException.class)
    public ResponseEntity<RestErrorMessage> handleInternalBusinessError(TranslationBridgeException ex,
            HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.",
                request);
    }

    private ResponseEntity<RestErrorMessage> buildResponseEntity(HttpStatus status, String message,
            HttpServletRequest request) {
        var path = (request != null) ? request.getRequestURI() : "Unknown";
        var errorBody = new RestErrorMessage(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);
        return ResponseEntity.status(status).body(errorBody);
    }
}