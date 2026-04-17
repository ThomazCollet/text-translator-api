package com.thomazcollet.text_translator_api.infra.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.thomazcollet.text_translator_api.exception.ExternalApiException;
import com.thomazcollet.text_translator_api.exception.InvalidAudioConfigException;
import com.thomazcollet.text_translator_api.exception.InvalidTextException;
import com.thomazcollet.text_translator_api.exception.LanguageNotSupportedException;
import com.thomazcollet.text_translator_api.exception.RestErrorMessage;
import com.thomazcollet.text_translator_api.exception.SpeechServiceException;
import com.thomazcollet.text_translator_api.exception.TextToSpeechLimitException;
import com.thomazcollet.text_translator_api.exception.TranslationBridgeException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Central de Tratamento de Erros da API.
 * Utiliza a semântica HTTP para traduzir exceções internas em respostas ricas e padronizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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

    // --- 1. SEGURANÇA E ERROS INESPERADOS ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.",
                request);
    }

    // --- 2. REGRAS DE NEGÓCIO E VALIDAÇÕES (HTTP 400) ---

    @ExceptionHandler(InvalidTextException.class)
    public ResponseEntity<RestErrorMessage> handleInvalidText(InvalidTextException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(LanguageNotSupportedException.class)
    public ResponseEntity<RestErrorMessage> handleLanguageNotSupported(LanguageNotSupportedException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(TextToSpeechLimitException.class)
    public ResponseEntity<RestErrorMessage> handleSpeechLimit(TextToSpeechLimitException ex, HttpServletRequest request) {
        // Indica que a solicitação não pode ser processada devido a limites de conteúdo
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidAudioConfigException.class)
    public ResponseEntity<RestErrorMessage> handleInvalidAudioConfig(InvalidAudioConfigException ex, HttpServletRequest request) {
        // Parâmetros técnicos inválidos enviados pelo cliente
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // --- 3. INFRAESTRUTURA E SERVIÇOS EXTERNOS (HTTP 502) ---

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<RestErrorMessage> handleExternalApi(ExternalApiException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(SpeechServiceException.class)
    public ResponseEntity<RestErrorMessage> handleSpeechService(SpeechServiceException ex, HttpServletRequest request) {
        // BAD_GATEWAY reflete falha no provedor de voz (VoiceRSS)
        return buildResponseEntity(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    // --- 4. FALHAS DE PROCESSO INTERNO (HTTP 500) ---

    @ExceptionHandler(TranslationBridgeException.class)
    public ResponseEntity<RestErrorMessage> handleTranslationBridge(TranslationBridgeException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}