package com.thomazcollet.text_translator_api.exception;

import java.time.LocalDateTime;

public record RestErrorMessage(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
){}
