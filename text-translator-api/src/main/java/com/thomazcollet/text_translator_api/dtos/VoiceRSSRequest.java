package com.thomazcollet.text_translator_api.dtos;

/**
 * Record que representa os parâmetros exigidos pela API do VoiceRSS.
 * Centraliza as configurações padrão de áudio (MP3, 22kHz) para evitar
 * duplicação.
 */
public record VoiceRSSRequest(
        String key,
        String src,
        String hl,
        String c, // Codec (ex: "MP3")
        String f, // Formato (ex: "22kHz_16bit_mono")
        String b64 // Base64 flag (ex: "true")
) {

    /**
     * Construtor de conveniência com os padrões de áudio otimizados para web.
     */
    public VoiceRSSRequest(String key, String src, String hl) {
        this(key, src, hl, "MP3", "22kHz_16bit_mono", "true");
    }
}