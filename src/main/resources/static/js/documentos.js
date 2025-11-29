let selectedDocumentId = null;
let currentDocuments = [];

async function fetchDocuments() {
    try {
        const res = await fetch('http://localhost:8082/documentos');

        if (!res.ok) {
            throw new Error(`Erro HTTP ${res.status}`);
        }

        const data = await res.json();

        if (!Array.isArray(data)) {
            throw new Error('Resposta do servidor não é um array de documentos');
        }

        currentDocuments = data;
        selectedDocumentId = currentDocuments.length > 0 ? currentDocuments[0].id : null;
        renderDocumentList(currentDocuments);
        renderDocumentDetails(getDocumentById(selectedDocumentId));
    } catch (err) {
        console.error('Erro ao buscar documentos:', err);
        const container = document.getElementById('document-list-container');
        if (container) {
            container.innerHTML = `<div class="text-center text-red-500 py-6">
                Erro ao buscar documentos: ${err.message}
            </div>`;
        }
    }
}

function getDocumentById(id) {
    return currentDocuments.find(doc => doc.id === id) || null;
}

function renderDocumentList(documents) {
    const container = document.getElementById('document-list-container');
    if (!container) return;

    if (!Array.isArray(documents) || documents.length === 0) {
        container.innerHTML = `<div class="text-center text-gray-500 py-6">
            Nenhum documento encontrado.
        </div>`;
        return;
    }

    container.innerHTML = documents.map(doc => {
        const isSelected = doc.id === selectedDocumentId;
        const selectedClass = isSelected ? 'bg-mocs-blue text-white shadow-md' : 'hover:bg-gray-50';

        return `
            <div class="p-3 border rounded-lg cursor-pointer ${selectedClass}" data-doc-id="${doc.id}">
                <p class="font-bold truncate">${doc.nome}</p>
                <p class="text-sm mt-1">Autor: ${doc.autor || 'Desconhecido'} | Status: ${doc.status}</p>
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

    let buttonsHTML = '';
    let avaliacaoHTML = '';

    if (doc.avaliacao) {
        avaliacaoHTML = `<div class="p-3 mt-2 border rounded bg-gray-50 text-gray-700">${doc.avaliacao}</div>`;
    }

    if (doc.status === 'CORRIGIR') {
        buttonsHTML = `
            <div class="inline-flex mb-2">
                <button id="download-button" data-doc-id="${doc.id}"
                    class="bg-mocs-blue text-white py-2 px-4 rounded inline-block mr-2">
                    Baixar
                </button>
                <button id="evaluate-button" data-doc-id="${doc.id}"
                    class="bg-mocs-orange text-white font-semibold py-2 px-4 rounded hover:bg-opacity-90 transition duration-150 mx-2">
                    Avaliar Documento
                </button>
            </div>
        `;
    } else if (doc.status === 'APRECIADO' || doc.status === 'APROVADO') {
        buttonsHTML = `
            <button id="download-button" data-doc-id="${doc.id}"
                class="bg-mocs-blue text-white py-2 px-4 rounded w-full">
                Baixar
            </button>
        `;
    } else {
        buttonsHTML = `
            <div class="inline-flex">
                <button id="download-button" data-doc-id="${doc.id}"
                    class="bg-mocs-blue text-white py-2 px-4 rounded inline-block mr-2">
                    Baixar
                </button>
                <button id="evaluate-button" data-doc-id="${doc.id}"
                    class="bg-mocs-orange text-white font-semibold py-2 px-4 rounded hover:bg-opacity-90 transition duration-150 mx-2">
                    Avaliar Documento
                </button>
            </div>
        `;
    }

    container.innerHTML = `
        <p class="font-bold text-lg mb-2">${doc.nome}</p>
        <p class="text-gray-600 mb-2">Autor: ${doc.autor || 'Desconhecido'}</p>
        <p class="text-gray-600 mb-2">Status: ${doc.status}${doc.isOfficial ? ' - Oficial' : ''}</p>
        ${buttonsHTML}
        ${avaliacaoHTML}
    `;

    document.getElementById('download-button')?.addEventListener('click', (e) => {
        downloadDocument(parseInt(e.currentTarget.dataset.docId));
    });

    if (doc.status === 'CORRIGIR' || doc.status === 'RECEBIDO') {
        document.getElementById('evaluate-button')?.addEventListener('click', (e) => {
            const docId = parseInt(e.currentTarget.dataset.docId);
            sessionStorage.setItem('documentIdToEvaluate', docId);
            window.location.href = `/avaliar_documentos.html?docId=${docId}`;
        });
    }
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

    const filtered = currentDocuments.filter(doc => {
        return (
            (!search) ||
            (doc.nome?.toLowerCase().includes(search)) ||
            (doc.autor?.toLowerCase().includes(search))
        );
    });

    selectedDocumentId = filtered.length > 0 ? filtered[0].id : null;
    renderDocumentList(filtered);
    renderDocumentDetails(getDocumentById(selectedDocumentId));
}

document.addEventListener('DOMContentLoaded', () => {
    fetchDocuments();
    const searchInput = document.getElementById('search-input');
    if (searchInput) searchInput.addEventListener('input', filterDocuments);
});
