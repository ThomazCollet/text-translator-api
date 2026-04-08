const translateURL = "http://localhost:8080/translate";
const textToSpeechURL = "http://localhost:8080/speech";

async function translateRequest(text, sourceLanguage, targetLanguage) {

    const response = await fetch(translateURL, {
        method: 'POST', // Mudamos para POST
        headers: {
            'Content-Type': 'application/json' // O "crachá" que avisa que é JSON
        },
        body: JSON.stringify({

            text: text,
            sourceLanguage: sourceLanguage,
            targetLanguage: targetLanguage
        })
    });

    if (response.ok) { // response.ok é um atalho para status entre 200-299
        const object = await response.json();
        return object; // Retornamos o objeto para quem chamou a função
    } else {
        console.error("Erro na tradução:", response.status);
        return null;
    }
}

async function speechRequest(textToSpeak, targetLanguage) { 
    // Note que usei 'textToSpeak' para bater com o nome que está no seu Service Java!
    const response = await fetch(textToSpeechURL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json' // O Java precisa disso para ler o seu @RequestBody
        },
        body: JSON.stringify({
            textToSpeak, 
            targetLanguage
        })
    });

    if (response.ok) {
        return await response.json(); // Aqui virá o seu SpeechResponse com o Base64
    }
    // ... erro
}
