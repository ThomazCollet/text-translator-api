// 1. Selecionando os elementos do DOM
const sourceText = document.getElementById('input-text');
const targetText = document.getElementById('output-text');
const sourceLang = document.getElementById('source-lang');
const targetLang = document.getElementById('target-lang');

const btnSwitch = document.getElementById('btn-switch');
const btnTranslate = document.getElementById('btn-translate');

const btnSpeakSource = document.getElementById('btn-listen-source');
const btnSpeakTarget = document.getElementById('btn-listen-target');

let audioPlayer = new Audio();
let debounceTimer; // Variável para controlar o tempo de espera

// --- Lógica de Tradução ---

const handleTranslate = async () => {
    // Agora que é automático, logs excessivos podem poluir o console, mas mantive para seu debug
    targetText.classList.add('loading-glow');
    const text = sourceText.value.trim();
    
    if (!text) {
        targetText.value = "";
        return;
    }

    targetText.placeholder = "Traduzindo...";

    const result = await translateRequest(text, sourceLang.value, targetLang.value);
    
    if (result && result.translatedText) {
        targetText.value = result.translatedText;

        // SINCRONIZAÇÃO: Se detectou idioma, atualiza o seletor
        if (result.sourceLanguage && sourceLang.value !== result.sourceLanguage) {
            console.log("Sincronizando idioma detectado:", result.sourceLanguage);
            sourceLang.value = result.sourceLanguage;
            handleLanguageChange(sourceLang);
        }

    } else {
        targetText.value = "Erro ao processar tradução.";
    }

    targetText.placeholder = "Tradução...";
    targetText.classList.remove('loading-glow');
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
        let base64String = result.audioBase64.trim().replace(/\s/g, '');

        if (!base64String.startsWith('data:')) {
            base64String = `data:audio/mp3;base64,${base64String}`;
        }

        audioPlayer.src = base64String;
        audioPlayer.play().catch(e => console.error("Erro no player:", e));
    } else {
        console.error("Áudio não encontrado na resposta do servidor.");
    }
};

// --- Event Listeners ---

// 2. NOVA LOGICA: Debounce para tradução automática
sourceText.addEventListener('input', () => {
    // Limpa o timer anterior se o usuário continuar digitando
    clearTimeout(debounceTimer);

    // Inicia um novo timer de 800ms
    debounceTimer = setTimeout(() => {
        handleTranslate();
    }, 800); 
});

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

sourceLang.addEventListener('change', () => {
    handleLanguageChange(sourceLang);
    handleTranslate(); // Traduz automaticamente se mudar o idioma
});

targetLang.addEventListener('change', () => {
    handleLanguageChange(targetLang);
    handleTranslate(); // Traduz automaticamente se mudar o idioma
});