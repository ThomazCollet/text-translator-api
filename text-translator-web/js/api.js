const translateURL = "http://localhost:8080/translate";
const textToSpeechURL = "http://localhost:8080/speech";

/**
 * Adicionamos o parâmetro 'signal' para permitir o cancelamento da requisição
 */
async function translateRequest(text, sourceLanguage, targetLanguage, signal) {

    const response = await fetch(translateURL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        signal: signal, // <--- ESTE É O PONTO CHAVE! Conecta com o AbortController
        body: JSON.stringify({
            text: text,
            sourceLanguage: sourceLanguage,
            targetLanguage: targetLanguage
        })
    });

    if (response.ok) {
        return await response.json(); 
    } else {
        // Se a requisição falhar por outros motivos (não cancelamento)
        console.error("Erro na resposta do servidor:", response.status);
        return null;
    }
}

async function speechRequest(textToSpeak, targetLanguage) { 
    const response = await fetch(textToSpeechURL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            textToSpeak, 
            targetLanguage
        })
    });

    if (response.ok) {
        return await response.json();
    }
    console.error("Erro na requisição de áudio:", response.status);
    return null;
}