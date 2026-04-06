package com.thomazcollet.text_translator_api.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thomazcollet.text_translator_api.dtos.SpeechRequest;
import com.thomazcollet.text_translator_api.dtos.SpeechResponse;
import com.thomazcollet.text_translator_api.service.TextToSpeechService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/speech")
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping
    public ResponseEntity<SpeechResponse> speech(@RequestBody @Valid SpeechRequest request) {
        SpeechResponse response = textToSpeechService.speech(request);

        return ResponseEntity.ok(response);
    }
}
