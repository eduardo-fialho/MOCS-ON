let selectedDocumentId = null;
let currentDocuments = [];

async function fetchDocuments() {
    try {
        const res = await fetch('http://localhost:8082/documentos');
        if (!res.ok) throw new Error(`Erro HTTP ${res.status}`);

        const data = await res.json();
        if (!Array.isArray(data)) throw new Error('Resposta inválida do servidor');

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
        container.innerHTML = `<div class="text-center text-slate-500 py-6">
            Nenhum documento encontrado.
        </div>`;
        return;
    }

    container.innerHTML = documents.map(doc => {
        const isSelected = doc.id === selectedDocumentId;
        const selectedClass = isSelected ? 'bg-blue-600 text-white shadow-md border-blue-600' : 'bg-white hover:bg-slate-50 border-slate-200';

        return `
            <div class="p-3 border rounded-lg cursor-pointer ${selectedClass}" data-doc-id="${doc.id}">
                <p class="font-bold truncate">${doc.nome}</p>
                <p class="text-sm mt-1 ${isSelected ? 'text-blue-50' : 'text-slate-600'}">
                    Autor: ${doc.autor || 'Desconhecido'} | Status: ${doc.status}
                </p>
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
        container.innerHTML = `<p class="text-slate-500">Selecione um documento.</p>`;
        return;
    }

    let buttonsHTML = '';
    let avaliacaoHTML = '';

    if (doc.avaliacao) {
        avaliacaoHTML = `<div class="p-3 mt-2 border rounded bg-slate-50 text-slate-700">${doc.avaliacao}</div>`;
    }

    if (doc.status === 'CORRIGIR') {
        buttonsHTML = `
            <div class="flex flex-wrap gap-2 mb-2">
                <button id="download-button" data-doc-id="${doc.id}"
                    class="bg-blue-600 text-white py-2 px-4 rounded shadow hover:bg-blue-700 transition">
                    Baixar
                </button>
                <button id="evaluate-button" data-doc-id="${doc.id}"
                    class="bg-amber-500 text-white font-semibold py-2 px-4 rounded shadow hover:bg-amber-600 transition">
                    Avaliar Documento
                </button>
            </div>
        `;
    } else if (doc.status === 'APRECIADO' || doc.status === 'APROVADO') {
        buttonsHTML = `
            <button id="download-button" data-doc-id="${doc.id}"
                class="bg-blue-600 text-white py-2 px-4 rounded shadow w-full hover:bg-blue-700 transition">
                Baixar
            </button>
        `;
    } else {
        buttonsHTML = `
            <div class="flex flex-wrap gap-2 mb-2">
                <button id="download-button" data-doc-id="${doc.id}"
                    class="bg-blue-600 text-white py-2 px-4 rounded shadow hover:bg-blue-700 transition">
                    Baixar
                </button>
                <button id="evaluate-button" data-doc-id="${doc.id}"
                    class="bg-amber-500 text-white font-semibold py-2 px-4 rounded shadow hover:bg-amber-600 transition">
                    Avaliar Documento
                </button>
            </div>
        `;
    }

    container.innerHTML = `
        <p class="font-bold text-lg mb-2 text-slate-900">${doc.nome}</p>
        <p class="text-slate-600 mb-2">Autor: ${doc.autor || 'Desconhecido'}</p>
        <p class="text-slate-600 mb-4">Status: ${doc.status}${doc.isOfficial ? ' - Oficial' : ''}</p>
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
