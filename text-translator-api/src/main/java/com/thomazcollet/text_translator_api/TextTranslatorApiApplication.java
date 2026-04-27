package com.thomazcollet.text_translator_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TextTranslatorApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextTranslatorApiApplication.class, args);
	}

}
