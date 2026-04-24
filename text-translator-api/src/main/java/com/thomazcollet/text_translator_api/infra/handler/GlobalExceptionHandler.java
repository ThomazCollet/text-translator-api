package com.thomazcollet.text_translator_api.infra.handler;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.thomazcollet.text_translator_api.exception.*;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<RestErrorMessage> buildResponseEntity(HttpStatus status, String message, HttpServletRequest request) {
        String path = (request != null) ? request.getRequestURI() : "Unknown";
        return ResponseEntity.status(status).body(new RestErrorMessage(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path));
    }

    // 1. REGRAS DE NEGÓCIO / VALIDAÇÕES (HTTP 400)
    @ExceptionHandler({
        InvalidTextException.class, 
        LanguageNotSupportedException.class, 
        TextToSpeechLimitException.class,
        InvalidAudioConfigException.class
    })
    public ResponseEntity<RestErrorMessage> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 2. INFRAESTRUTURA / SERVIÇOS EXTERNOS (HTTP 502)
    @ExceptionHandler({
        ExternalApiException.class, 
        SpeechServiceException.class
    })
    public ResponseEntity<RestErrorMessage> handleBadGateway(RuntimeException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    // 3. FALHAS INTERNAS ESPECÍFICAS (HTTP 500)
    @ExceptionHandler(TranslationBridgeException.class)
    public ResponseEntity<RestErrorMessage> handleInternalBusinessError(TranslationBridgeException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // 4. FALLBACK GENÉRICO (HTTP 500) - O "seguro morreu de velho"
    // Mantemos por último para pegar qualquer erro que não mapeamos acima
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.",
                request);
    }
}