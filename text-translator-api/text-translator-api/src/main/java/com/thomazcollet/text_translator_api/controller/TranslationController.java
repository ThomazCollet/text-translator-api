package com.thomazcollet.text_translator_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thomazcollet.text_translator_api.dtos.TextRequest;
import com.thomazcollet.text_translator_api.dtos.TextResponse;
import com.thomazcollet.text_translator_api.service.TranslationService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/translate")
public class TranslationController {
   
    private final TranslationService translationService;
    public TranslationController(TranslationService translationService){
        this.translationService = translationService;
    }

    @PostMapping
    public TextResponse translate(@RequestBody @Valid TextRequest request){

        return translationService.translate(request);
    }

}
