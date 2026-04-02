package com.thomazcollet.text_translator_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TextToSpeechService {
    
    private final RestTemplate restTemplate;
    
    public TextToSpeechService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
}
