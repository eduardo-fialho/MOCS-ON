function getDocumentById(id) {
    return JSON.parse(sessionStorage.getItem('currentDocuments') || '[]').find(d => d.id === id);
}

function loadDocumentForEvaluation() {
    const docId = parseInt(sessionStorage.getItem('documentIdToEvaluate'));
    const doc = getDocumentById(docId);
    const titleEl = document.getElementById('doc-title-view');
    const contentEl = document.getElementById('doc-content-placeholder');

    if (doc) {
        titleEl.textContent = doc.nome;
        contentEl.innerHTML = `<i class="fas fa-file-pdf mr-2"></i> Visualizando: ${doc.nome} (Autor: ${doc.autor})`;
    } else {
        titleEl.textContent = 'Documento não encontrado';
        contentEl.innerHTML = 'ID do documento inválido.';
        document.getElementById('evaluation-form').style.display = 'none';
    }
}

function displayMessage(text, isSuccess) {
    const area = document.getElementById('message-area');
    area.textContent = text;
    area.style.display = 'block';
    area.className = isSuccess ? 'bg-green-100 text-green-700 p-3 rounded text-center' : 'bg-red-100 text-red-700 p-3 rounded text-center';
}

function handleEvaluationSubmit(e) {
    e.preventDefault();
    const docId = parseInt(sessionStorage.getItem('documentIdToEvaluate'));
    const status = document.getElementById('status-select-eval').value;
    const isOfficial = document.querySelector('input[name="isOfficial"]:checked').value === 'true';
    const comments = document.getElementById('reviewer-comments').value;

    if (!status || !comments) return displayMessage('Preencha todos os campos.', false);

    const data = { documentId: docId, status, isOfficial, comments, avaliador: 'Secretário de Comitê' };
    console.log('Avaliação enviada:', data);
    displayMessage(`Avaliação salva! Novo status: ${status}.`, true);

    document.getElementById('evaluation-form').reset();
    sessionStorage.removeItem('documentIdToEvaluate');
}

document.addEventListener('DOMContentLoaded', () => {
    loadDocumentForEvaluation();
    document.getElementById('evaluation-form').addEventListener('submit', handleEvaluationSubmit);
});
