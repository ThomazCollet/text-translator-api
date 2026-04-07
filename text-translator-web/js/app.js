// 1. Selecionando os elementos do DOM
const sourceText = document.getElementById('input-text');
const targetText = document.getElementById('output-text');
const sourceLang = document.getElementById('source-lang');
const targetLang = document.getElementById('target-lang');
const btnSwitch = document.getElementById('btn-switch');
const btnTranslate = document.getElementById('btn-translate'); // Selecionando o botão de traduzir

// --- Lógica de Tradução ---

/**
 * Função principal que coordena a tradução entre UI e API
 */
const handleTranslate = async () => {
    const text = sourceText.value.trim();
    
    if (!text) {
        targetText.value = "";
        return;
    }

    // Feedback visual (opcional, mas bom para UX)
    targetText.placeholder = "Traduzindo...";

    // Chamamos a função que você criou no api.js
    // Note que usamos os nomes sourceLanguage e targetLanguage para bater com o Java
    const result = await translateRequest(text, sourceLang.value, targetLang.value);

    if (result && result.translatedText) {
        targetText.value = result.translatedText;
    } else {
        // Caso a API falhe, damos um feedback ao usuário
        targetText.value = "Erro ao processar tradução.";
    }
    
    targetText.placeholder = "Tradução...";
};

// --- Event Listeners ---

// Botão de Inverter
btnSwitch.addEventListener('click', () => {
    const tempLang = sourceLang.value;
    const currentTargetText = targetText.value;

    if (tempLang !== 'AUTO') {
        sourceLang.value = targetLang.value;
        targetLang.value = tempLang;
    } else {
        sourceLang.value = targetLang.value;
        targetLang.value = 'PT'; 
    }

    if (currentTargetText.trim() !== "") {
        sourceText.value = currentTargetText;
        targetText.value = ""; 
        
        // Agora ativamos a tradução automática ao inverter!
        handleTranslate(); 
    } else {
        const tempInput = sourceText.value;
        sourceText.value = targetText.value;
        targetText.value = tempInput;
    }
});

// Botão Traduzir
btnTranslate.addEventListener('click', handleTranslate);

// Bônus: Traduzir ao apertar Enter (Ctrl + Enter para não quebrar linha)
sourceText.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
        e.preventDefault();
        handleTranslate();
    }
});

// --- Lógica de Seletores (Sincronização) ---

const handleLanguageChange = (changedElement) => {
    const sourceValue = sourceLang.value;
    const targetValue = targetLang.value;

    if (sourceValue === targetValue && sourceValue !== 'AUTO') {
        if (changedElement === sourceLang) {
            targetLang.value = (sourceValue === 'PT') ? 'EN' : 'PT';
        } else {
            sourceLang.value = (targetValue === 'PT') ? 'EN' : 'PT';
        }
    }
};

sourceLang.addEventListener('change', () => handleLanguageChange(sourceLang));
targetLang.addEventListener('change', () => handleLanguageChange(targetLang));