// 1. Selecionando os elementos do DOM
const sourceText = document.getElementById('input-text');
const targetText = document.getElementById('output-text');
const sourceLang = document.getElementById('source-lang');
const targetLang = document.getElementById('target-lang');

const btnSwitch = document.getElementById('btn-switch');
const btnSpeakSource = document.getElementById('btn-listen-source');
const btnSpeakTarget = document.getElementById('btn-listen-target');

let audioPlayer = new Audio();
let debounceTimer; 
let currentTranslateController = null; // --- NOVO: Controle para cancelar requisições ---

// --- Lógica de Tradução ---

/**
 * Realiza a tradução automática com feedback visual e cancelamento de requests pendentes
 */
const handleTranslate = async () => {
    const text = sourceText.value.trim();

    // Se o campo estiver vazio, limpa a saída e interrompe
    if (!text) {
        targetText.value = "";
        return;
    }

    // --- NOVO: Lógica do Cancelador ---
    // Se já houver uma requisição de tradução voando, a gente cancela ela aqui
    if (currentTranslateController) {
        currentTranslateController.abort();
    }
    // Criamos um novo controlador para a requisição atual
    currentTranslateController = new AbortController();
    const signal = currentTranslateController.signal;
    // ----------------------------------

    // Início do feedback visual
    targetText.classList.add('loading-glow');
    targetText.placeholder = "Traduzindo...";

    try {
        // IMPORTANTE: Passe o 'signal' para a sua função translateRequest (ajuste ela se necessário)
        const result = await translateRequest(text, sourceLang.value, targetLang.value, signal);

        if (result && result.translatedText) {
            targetText.value = result.translatedText;

            // Sincronização automática caso o idioma seja detectado
            if (result.sourceLanguage && sourceLang.value !== result.sourceLanguage) {
                console.log("Idioma detectado e sincronizado:", result.sourceLanguage);
                sourceLang.value = result.sourceLanguage;
                handleLanguageChange(sourceLang);
                handleTranslate();
            }
        } else {
            targetText.value = "Erro ao processar tradução.";
        }
    } catch (error) {
        // Se o erro for apenas o cancelamento, não fazemos nada (UX limpa)
        if (error.name === 'AbortError') {
            console.log("Requisição anterior cancelada.");
            return; 
        }
        console.error("Erro na tradução:", error);
    } finally {
        // Só removemos o feedback visual se esta for a requisição mais recente
        if (!signal.aborted) {
            targetText.placeholder = "Tradução...";
            targetText.classList.remove('loading-glow');
            currentTranslateController = null;
        }
    }
};

/**
 * Lida com a geração e reprodução de áudio com bloqueio de botão
 */
const handleSpeech = async (textArea, langSelect, btnElement) => {
    const text = textArea.value.trim();
    const language = langSelect.value;

    if (!text || language === 'AUTO') {
        alert("Selecione um idioma para ouvir.");
        return;
    }

    btnElement.style.opacity = "0.5";
    btnElement.style.cursor = "wait";
    btnElement.style.pointerEvents = "none";

    try {
        const result = await speechRequest(text, language);

        if (result && result.audioBase64) {
            let base64String = result.audioBase64.trim().replace(/\s/g, '');
            if (!base64String.startsWith('data:')) {
                base64String = `data:audio/mp3;base64,${base64String}`;
            }
            audioPlayer.src = base64String;
            audioPlayer.play().catch(e => console.error("Erro no player:", e));
        } else {
            console.error("Áudio não encontrado na resposta.");
        }
    } catch (error) {
        console.error("Erro na requisição de áudio:", error);
    } finally {
        btnElement.style.opacity = "1";
        btnElement.style.cursor = "pointer";
        btnElement.style.pointerEvents = "auto";
    }
};

/**
 * Garante que os idiomas de origem e destino não sejam iguais
 */
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

// --- Event Listeners ---

sourceText.addEventListener('input', () => {
    clearTimeout(debounceTimer);
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

btnSpeakSource.addEventListener('click', (e) => handleSpeech(sourceText, sourceLang, e.currentTarget));
btnSpeakTarget.addEventListener('click', (e) => handleSpeech(targetText, targetLang, e.currentTarget));

sourceText.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
        e.preventDefault();
        handleTranslate();
    }
});

sourceLang.addEventListener('change', () => {
    handleLanguageChange(sourceLang);
    handleTranslate();
});

targetLang.addEventListener('change', () => {
    handleLanguageChange(targetLang);
    handleTranslate();
});