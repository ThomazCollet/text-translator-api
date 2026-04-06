// Selecionando os elementos do DOM
const sourceText = document.getElementById('input-text');
const targetText = document.getElementById('output-text');
const sourceLang = document.getElementById('source-lang');
const targetLang = document.getElementById('target-lang');
const btnSwitch = document.getElementById('btn-switch');

btnSwitch.addEventListener('click', () => {
    const tempLang = sourceLang.value;
    const currentTargetText = targetText.value;

    // 1. Inverter Idiomas (Garantindo que o destino não vire "AUTO")
    if (tempLang !== 'AUTO') {
        sourceLang.value = targetLang.value;
        targetLang.value = tempLang;
    } else {
        // Se a origem era AUTO, invertemos apenas o que está no destino para a origem
        sourceLang.value = targetLang.value;
        targetLang.value = 'PT'; // Definimos um padrão (Português) para o novo destino
    }

    // 2. Mover o texto da tradução para o campo de entrada
    // Só movemos se houver algo para mover, senão apenas limpamos
    if (currentTargetText.trim() !== "") {
        sourceText.value = currentTargetText;
        targetText.value = ""; // Limpa a direita enquanto aguarda a nova tradução
        
        // 3. Disparar a tradução (Chamaremos a função da API aqui em breve)
        console.log("Chamando tradução inversa...");
        // translateAction(); // <-- Essa função criaremos no api.js
    } else {
        // Se não tinha tradução, apenas inverte o que o usuário já tinha digitado
        const tempInput = sourceText.value;
        sourceText.value = targetText.value;
        targetText.value = tempInput;
    }
});

// Função para garantir que os idiomas não sejam iguais
const handleLanguageChange = (changedElement) => {
    const sourceValue = sourceLang.value;
    const targetValue = targetLang.value;

    // Se a origem for igual ao destino (e não for AUTO)
    if (sourceValue === targetValue && sourceValue !== 'AUTO') {
        // Se mudamos a ORIGEM para Inglês, o DESTINO vira Português (ou vice-versa)
        if (changedElement === sourceLang) {
            targetLang.value = (sourceValue === 'PT') ? 'EN' : 'PT';
        } else {
            sourceLang.value = (targetValue === 'PT') ? 'EN' : 'PT';
        }
    }
};

// Adicionando os "ouvidores" de mudança (Change Events)
sourceLang.addEventListener('change', () => handleLanguageChange(sourceLang));
targetLang.addEventListener('change', () => handleLanguageChange(targetLang));