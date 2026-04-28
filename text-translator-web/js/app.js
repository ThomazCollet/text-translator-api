/**
 * BRIDGE TRANSLATE - CORE APPLICATION
 * Gerencia a interface, tradução e síntese de voz.
 */

// 1. Seleção de elementos do DOM
const sourceText = document.getElementById('input-text');
const targetText = document.getElementById('output-text');
const sourceLang = document.getElementById('source-lang');
const targetLang = document.getElementById('target-lang');

const btnSwitch = document.getElementById('btn-switch');
const btnCopyTarget = document.getElementById('btn-copy-target');
const btnSpeakSource = document.getElementById('btn-listen-source');
const btnSpeakTarget = document.getElementById('btn-listen-target');

// Configurações globais
let audioPlayer = new Audio();
let debounceTimer;
let currentTranslateController = null;

// --- Lógica de Tradução ---

/**
 * Realiza a tradução automática com feedback visual e cancelamento de requests pendentes.
 */
// No topo, selecione o novo elemento
const loader = document.getElementById('loader');

/**
 * Realiza a tradução automática com feedback visual e cancelamento de requests pendentes.
 */
const handleTranslate = async () => {
    const text = sourceText.value.trim();

    if (!text) {
        targetText.value = "";
        loader.style.display = 'none'; // Garante que o loader suma se limpar o texto
        return;
    }

    if (currentTranslateController) {
        currentTranslateController.abort();
    }
    currentTranslateController = new AbortController();
    const signal = currentTranslateController.signal;

    // INÍCIO DO FEEDBACK VISUAL
    targetText.classList.add('loading-glow');
    loader.style.display = 'flex'; // Mostra os pontinhos
    document.body.classList.add('waiting'); // Cursor de loading

    if (!targetText.value) {
        targetText.placeholder = "Traduzindo...";
    }

    try {
        const result = await translateRequest(text, sourceLang.value, targetLang.value, signal);

        if (result && result.translatedText) {
            targetText.value = result.translatedText;

            if (result.sourceLanguage && sourceLang.value !== result.sourceLanguage) {
                sourceLang.value = result.sourceLanguage;
                handleLanguageChange(sourceLang);
                handleTranslate();
            }
        } else {
            targetText.value = "Erro ao processar tradução.";
        }
    } catch (error) {
        if (error.name === 'AbortError') return;
        console.error("Erro na tradução:", error);
    } finally {
        if (!signal.aborted) {
            // FIM DO FEEDBACK VISUAL
            targetText.placeholder = "...";
            targetText.classList.remove('loading-glow');
            loader.style.display = 'none'; // Esconde os pontinhos
            document.body.classList.remove('waiting'); // Cursor volta ao normal
            currentTranslateController = null;
        }
    }
};

// --- Lógica de Interface & Utilidades ---

/**
 * Copia o texto traduzido para o clipboard com feedback visual de sucesso.
 */
const handleCopy = async () => {
    const text = targetText.value.trim();

    // Impede cópia se não houver conteúdo válido
    if (!text || text === "...") return;

    try {
        await navigator.clipboard.writeText(text);

        // Feedback visual: Troca ícone e cor
        btnCopyTarget.classList.add('btn-copy-success');
        const icon = btnCopyTarget.querySelector('i');
        icon.classList.replace('fa-copy', 'fa-check');

        setTimeout(() => {
            btnCopyTarget.classList.remove('btn-copy-success');
            icon.classList.replace('fa-check', 'fa-copy');
        }, 2000);
    } catch (err) {
        console.error('Falha ao copiar:', err);
    }
};

/**
 * Lida com a geração e reprodução de áudio (TTS).
 */
const handleSpeech = async (textArea, langSelect, btnElement) => {
    const text = textArea.value.trim();
    const language = langSelect.value;

    if (!text || language === 'AUTO') {
        alert("Selecione um idioma para ouvir.");
        return;
    }

    // Lock temporário do botão
    btnElement.style.opacity = "0.5";
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
        }
    } catch (error) {
        console.error("Erro na requisição de áudio:", error);
    } finally {
        btnElement.style.opacity = "1";
        btnElement.style.pointerEvents = "auto";
    }
};

/**
 * Garante a consistência entre idioma de origem e destino.
 */
const handleLanguageChange = (changedElement) => {
    if (sourceLang.value === targetLang.value && sourceLang.value !== 'AUTO') {
        if (changedElement === sourceLang) {
            targetLang.value = (sourceLang.value === 'PT_BR') ? 'EN' : 'PT_BR';
        } else {
            sourceLang.value = (targetLang.value === 'PT_BR') ? 'EN' : 'PT_BR';
        }
    }
};

// --- Event Listeners ---

sourceText.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(handleTranslate, 800);
});

btnSwitch.addEventListener('click', () => {
    const tempLang = sourceLang.value;
    const currentTargetText = targetText.value;

    sourceLang.value = (tempLang !== 'AUTO') ? targetLang.value : targetLang.value;
    targetLang.value = (tempLang !== 'AUTO') ? tempLang : 'PT_BR';

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

btnCopyTarget.addEventListener('click', handleCopy);

btnSpeakSource.addEventListener('click', (e) => handleSpeech(sourceText, sourceLang, e.currentTarget));
btnSpeakTarget.addEventListener('click', (e) => handleSpeech(targetText, targetLang, e.currentTarget));

sourceLang.addEventListener('change', () => {
    handleLanguageChange(sourceLang);
    handleTranslate();
});

targetLang.addEventListener('change', () => {
    handleLanguageChange(targetLang);
    handleTranslate();
});

// Suporte para atalho Ctrl+Enter para traduzir manualmente
sourceText.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
        e.preventDefault();
        handleTranslate();
    }
});