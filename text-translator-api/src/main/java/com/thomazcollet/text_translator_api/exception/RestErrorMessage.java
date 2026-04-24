package com.thomazcollet.text_translator_api.exception;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RestErrorMessage(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("timestamp")
    LocalDateTime timestamp,
    
    @JsonProperty("status")
    int status,
    
    @JsonProperty("error")
    String error,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("path")
    String path
) {}