package com.thomazcollet.text_translator_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidTextException extends RuntimeException {
    public InvalidTextException() {
        super("O texto fornecido para tradução é inválido ou está vazio.");
    }

    public InvalidTextException(String message) {
        super(message);
    }
}