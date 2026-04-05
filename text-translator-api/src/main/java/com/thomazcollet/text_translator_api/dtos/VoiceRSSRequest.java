package com.thomazcollet.text_translator_api.dtos;

public record VoiceRSSRequest(
    String key,
    String src,
    String hl,
    String c,     // "MP3"
    String f,     // "22kHz_16bit_mono"
    String b64    // "true"
) {
    
    public VoiceRSSRequest(String key, String src, String hl) {
        this(key, src, hl, "MP3", "22kHz_16bit_mono", "true");
    }
}