function displayMessage(text, isSuccess) {
    const area = document.getElementById('message-area');
    if (!area) return;
    area.textContent = text;
    area.style.display = 'block';
    area.className = isSuccess
        ? 'bg-green-100 text-green-700 p-3 rounded text-center'
        : 'bg-red-100 text-red-700 p-3 rounded text-center';
}

function handleEvaluationSubmit(e) {
    e.preventDefault();

    const docId = parseInt(sessionStorage.getItem('documentIdToEvaluate'));
    const status = document.getElementById('status-select-eval')?.value;
    const isOfficial = document.querySelector('input[name="isOfficial"]:checked')?.value === 'true';
    const comments = document.getElementById('reviewer-comments')?.value;

    if (!status || !comments) {
        return displayMessage('Preencha todos os campos.', false);
    }

    const avaliador = document.querySelector('[x-data="profileData()"] [x-text="userName"]')?.textContent || 'Usuário Desconhecido';

    const data = {
        documentId: docId,
        status,
        isOfficial,
        comments,
        avaliador
    };

    fetch(`http://localhost:8082/documentos/${docId}/avaliar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(resData => displayMessage(`Avaliação salva! Novo status: ${resData.status}.`, true))
    .catch(err => displayMessage('Erro ao salvar avaliação.', false));
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('evaluation-form');
    
    const docId = parseInt(sessionStorage.getItem('documentIdToEvaluate'));
    if (docId) {
        visualizarDocumento(docId);
    }
    
    if (form) {
        form.addEventListener('submit', handleEvaluationSubmit);
    }
});

async function visualizarDocumento(docId) {
    const iframe = document.getElementById('viewer-iframe');
    const message = document.getElementById('viewer-message');
    const downloadLink = document.getElementById('download-link');
    iframe.classList.add('hidden');
    message.classList.add('hidden');
    downloadLink.classList.add('hidden');

    try {
        const response = await fetch(`/documentos/${docId}/visualizar`);
        if (!response.ok) {
            message.textContent = "Erro ao carregar o documento.";
            message.classList.remove('hidden');
            return;
        }

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        iframe.src = url;
        iframe.classList.remove('hidden');

    } catch (err) {
        console.error(err);
        message.textContent = "Erro ao carregar o documento.";
        message.classList.remove('hidden');
    }
}