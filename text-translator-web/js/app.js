// 1. Selecionando os elementos do DOM
const sourceText = document.getElementById('input-text');
const targetText = document.getElementById('output-text');
const sourceLang = document.getElementById('source-lang');
const targetLang = document.getElementById('target-lang');

const btnSwitch = document.getElementById('btn-switch');
const btnTranslate = document.getElementById('btn-translate');

// AJUSTADO: Usando os IDs exatos que estão no seu HTML
const btnSpeakSource = document.getElementById('btn-listen-source');
const btnSpeakTarget = document.getElementById('btn-listen-target');

// Objeto global para tocar o som
let audioPlayer = new Audio(); 

// --- Lógica de Tradução ---

const handleTranslate = async () => {
    const text = sourceText.value.trim();
    if (!text) {
        targetText.value = "";
        return;
    }

    targetText.placeholder = "Traduzindo...";
    const result = await translateRequest(text, sourceLang.value, targetLang.value);

    if (result && result.translatedText) {
        targetText.value = result.translatedText;
    } else {
        targetText.value = "Erro ao processar tradução.";
    }
    targetText.placeholder = "Tradução...";
};

/**
 * Lida com a reprodução de áudio
 */
const handleSpeech = async (textArea, langSelect) => {
    const text = textArea.value.trim();
    const language = langSelect.value;

    if (!text || language === 'AUTO') {
        alert("Selecione um idioma para ouvir.");
        return;
    }

    const result = await speechRequest(text, language);

    if (result && result.audioBase64) {
        let base64String = result.audioBase64;

        // Limpeza de segurança: remove espaços ou quebras de linha
        base64String = base64String.trim().replace(/\s/g, '');

        // Verificação: Adiciona prefixo se não existir
        if (!base64String.startsWith('data:')) {
            base64String = `data:audio/mp3;base64,${base64String}`;
        }

        audioPlayer.src = base64String;
        
        audioPlayer.play().catch(e => {
            console.error("Erro no player:", e);
            alert("Erro ao reproduzir. Verifique se o texto é muito longo ou se a API Key é válida.");
        });
    } else {
        console.error("Áudio não encontrado na resposta do servidor.");
    }
}; // <--- O fechamento correto é aqui!

// --- Event Listeners ---

btnSwitch.addEventListener('click', () => {
    const tempLang = sourceLang.value;
    const currentTargetText = targetText.value;

    if (tempLang !== 'AUTO') {
        sourceLang.value = targetLang.value;
        targetLang.value = tempLang;
    } else {
        sourceLang.value = targetLang.value;
        targetLang.value = 'PT_BR'; 
    }

    if (currentTargetText.trim() !== "") {
        sourceText.value = currentTargetText;
        targetText.value = ""; 
        handleTranslate(); 
    } else {
        const tempInput = sourceText.value;
        sourceText.value = targetText.value;
        targetText.value = tempInput;
    }
});

btnTranslate.addEventListener('click', handleTranslate);

// Ouvintes para os botões de áudio
btnSpeakSource.addEventListener('click', () => handleSpeech(sourceText, sourceLang));
btnSpeakTarget.addEventListener('click', () => handleSpeech(targetText, targetLang));

sourceText.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
        e.preventDefault();
        handleTranslate();
    }
});

// --- Lógica de Sincronização de Idiomas ---

const handleLanguageChange = (changedElement) => {
    const sourceValue = sourceLang.value;
    const targetValue = targetLang.value;

    if (sourceValue === targetValue && sourceValue !== 'AUTO') {
        if (changedElement === sourceLang) {
            targetLang.value = (sourceValue === 'PT_BR') ? 'EN' : 'PT_BR';
        } else {
            sourceLang.value = (targetValue === 'PT_BR') ? 'EN' : 'PT_BR';
        }
    }
};

sourceLang.addEventListener('change', () => handleLanguageChange(sourceLang));
targetLang.addEventListener('change', () => handleLanguageChange(targetLang));