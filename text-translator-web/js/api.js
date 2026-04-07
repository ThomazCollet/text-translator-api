const translateURL = "http://localhost:8080/translate";

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
