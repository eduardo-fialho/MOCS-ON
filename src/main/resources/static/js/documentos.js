let selectedDocumentId = null;
let currentDocuments = [];

function formatDate(isoDate) {
    const options = { year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(isoDate).toLocaleDateString('pt-BR', options);
}

async function fetchDocuments() {
    try {
        const res = await fetch('/documentos/todos');
        currentDocuments = await res.json();
        selectedDocumentId = currentDocuments.length > 0 ? currentDocuments[0].id : null;
        renderDocumentList(currentDocuments);
        renderDocumentDetails(getDocumentById(selectedDocumentId));
    } catch (err) {
        console.error('Erro ao buscar documentos:', err);
    }
}

function getDocumentById(id) {
    return currentDocuments.find(doc => doc.id === id);
}

function renderDocumentList(documents) {
    const container = document.getElementById('document-list-container');
    if (!container) return;
    if (documents.length === 0) {
        container.innerHTML = `<div class="text-center text-gray-500 py-6">Nenhum documento encontrado.</div>`;
        return;
    }

    container.innerHTML = documents.map(doc => {
        const isSelected = doc.id === selectedDocumentId;
        const selectedClass = isSelected ? 'bg-mocs-blue text-white shadow-md' : 'hover:bg-gray-50';
        return `
            <div class="p-3 border rounded-lg cursor-pointer ${selectedClass}" data-doc-id="${doc.id}">
                <p class="font-bold truncate">${doc.nome}</p>
                <p class="text-sm text-gray-600 mt-1">Autor: ${doc.autor} | Status: ${doc.status}</p>
            </div>
        `;
    }).join('');

    container.querySelectorAll('[data-doc-id]').forEach(el => {
        el.addEventListener('click', () => selectDocument(parseInt(el.dataset.docId)));
    });
}

function renderDocumentDetails(doc) {
    const container = document.getElementById('details-view-container');
    if (!container) return;
    if (!doc) {
        container.innerHTML = `<p class="text-gray-500">Selecione um documento.</p>`;
        return;
    }

    container.innerHTML = `
        <p class="font-bold text-lg mb-2">${doc.nome}</p>
        <p class="text-gray-600 mb-2">Autor: ${doc.autor}</p>
        <p class="text-gray-600 mb-2">Status: ${doc.status}</p>
        <button id="download-button" data-doc-id="${doc.id}" class="bg-mocs-blue text-white py-2 px-4 rounded">Baixar</button>
    `;

    document.getElementById('download-button').addEventListener('click', (e) => {
        downloadDocument(parseInt(e.currentTarget.dataset.docId));
    });
}

function selectDocument(id) {
    selectedDocumentId = id;
    renderDocumentList(currentDocuments);
    renderDocumentDetails(getDocumentById(id));
}

function downloadDocument(docId) {
    const link = document.createElement('a');
    link.href = `/documentos/${docId}/download`;
    link.download = '';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function filterDocuments() {
    const search = document.getElementById('search-input').value.toLowerCase();
    const type = document.getElementById('type-select').value;
    const status = document.getElementById('status-select').value;

    const filtered = currentDocuments.filter(doc => {
        return (!search || doc.nome.toLowerCase().includes(search) || doc.autor.toLowerCase().includes(search)) &&
               (!type || doc.tipo === type) &&
               (!status || doc.status === status);
    });

    selectedDocumentId = filtered.length > 0 ? filtered[0].id : null;
    renderDocumentList(filtered);
    renderDocumentDetails(getDocumentById(selectedDocumentId));
}

document.addEventListener('DOMContentLoaded', () => {
    fetchDocuments();

    document.getElementById('search-input').addEventListener('input', filterDocuments);
    document.getElementById('type-select').addEventListener('change', filterDocuments);
    document.getElementById('status-select').addEventListener('change', filterDocuments);
});
