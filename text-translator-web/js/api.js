/**
 * API SERVICE LAYER - BRIDGE TRANSLATE
 * Gerencia a comunicação assíncrona com o backend Spring Boot.
 */

const API_BASE_URL = "http://localhost:8080";

const API_ENDPOINTS = {
    TRANSLATE: `${API_BASE_URL}/translate`,
    SPEECH: `${API_BASE_URL}/speech`
};

/**
 * Envia uma solicitação de tradução.
 * @param {string} text - Texto original.
 * @param {string} sourceLanguage - Idioma origem.
 * @param {string} targetLanguage - Idioma destino.
 * @param {AbortSignal} signal - Sinal para cancelamento.
 */
async function translateRequest(text, sourceLanguage, targetLanguage, signal) {
    try {
        const response = await fetch(API_ENDPOINTS.TRANSLATE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            signal: signal,
            body: JSON.stringify({ text, sourceLanguage, targetLanguage })
        });

        if (!response.ok) throw new Error(`Erro HTTP: ${response.status}`);
        return await response.json();
    } catch (error) {
        if (error.name === 'AbortError') return null;
        console.error("Falha na tradução:", error.message);
        return null;
    }
}

/**
 * Envia uma solicitação de Text-to-Speech.
 * @param {string} textToSpeak - Conteúdo para voz.
 * @param {string} targetLanguage - Idioma da voz.
 */
async function speechRequest(textToSpeak, targetLanguage) {
    try {
        const response = await fetch(API_ENDPOINTS.SPEECH, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ textToSpeak, targetLanguage })
        });

        if (!response.ok) throw new Error(`Erro HTTP: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("Falha na síntese de voz:", error.message);
        return null;
    }
}