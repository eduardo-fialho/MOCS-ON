let selectedMateriaId = null;
let currentMaterias = [];

function timeAgo(dateString) {
    try {
        const d = new Date(dateString);
        const now = new Date();
        const s = Math.floor((now - d) / 1000);
        if (s < 60) return `${s} segundos atrás`;
        const m = Math.floor(s / 60);
        if (m < 60) return `${m} minutos atrás`;
        const h = Math.floor(m / 60);
        if (h < 24) return `${h} horas atrás`;
        const days = Math.floor(h / 24);
        if (days < 7) return `${days} dias atrás`;
        return d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch (e) { return 'data inválida'; }
}

async function fetchMaterias() {
    try {
        const res = await fetch('/imprensa/materias', {
            credentials: 'include'

        });

        if (!res.ok) {
            throw new Error(`Erro HTTP ${res.status}`);
        }

        const data = await res.json();
        console.log('Resposta do backend:', data);

        if (!Array.isArray(data)) {
            throw new Error('Resposta inválida do servidor');
        }

        currentMaterias = data;

        const todas = currentMaterias;

        const pendentes = currentMaterias.filter(m => m.status === 'PENDENTE');
        const aprovadas = currentMaterias.filter(m => m.status === 'APROVADA');

        renderLista('lista-todas', todas);
        renderLista('lista-pendentes', pendentes);
        renderLista('lista-aprovadas', aprovadas);

        selectedMateriaId = todas.length ? todas[0].id : null;
        renderMateriaDetails(getMateriaById(selectedMateriaId));

    } catch (err) {
        console.error('Erro ao buscar matérias:', err);
    }
}

function getMateriaById(id) {
    return currentMaterias.find(m => m.id === id) || null;
}

function renderLista(containerId, materias) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!materias.length) {
        container.innerHTML = `
            <p class="text-sm text-slate-400 italic">
                Nenhuma matéria.
            </p>
        `;
        return;
    }

    container.innerHTML = materias.map(m => `
        <div class="border border-slate-200 rounded-xl p-4 hover:bg-slate-50 cursor-pointer"
             onclick="carregarDetalhes(${m.id})">

            <div class="flex justify-between items-center">
                <h3 class="font-semibold text-slate-900">
                    ${m.titulo}
                </h3>

                <span class="text-xs px-2 py-1 rounded-full ${m.status === 'PENDENTE'
            ? 'bg-amber-100 text-amber-700'
            : 'bg-green-100 text-green-700'
        }">
                    ${m.status}
                </span>
            </div>

            <div class="text-sm text-slate-600 mt-1 line-clamp-2">
                Por: ${ m.autor ?? 'Autor desconhecido'}
            </div>

            <p class="text-xs text-slate-500 mt-2">
                ${timeAgo(m.createdAt) ?? ''}
            </p>

        </div>
    `).join('');
}

function renderMateriaDetails(m) {
    const c = document.getElementById('details-view-container');
    if (!c || !m) return;

    const usuarioLogado = window.usuarioLogadoEmail;
    const podeAprovar =
        m.status === 'PENDENTE' &&
        m.autor !== usuarioLogado;

    c.innerHTML = `
        <h3 class="text-xl font-bold">${m.titulo}</h3>

        <p class="text-sm text-slate-500">
            Autor: <strong>${m.autor}</strong>
        </p>

        <p class="text-sm text-slate-500">
            Criado em: ${timeAgo(m.createdAt)}
        </p>

        ${m.reviewedAt ? `
            <p class="text-sm text-slate-500">
                Revisado por: <strong>${m.revisor}</strong><br>
                Em: ${timeAgo(m.reviewedAt)}
            </p>
        ` : ''}

        <div class="mt-4">
            <p class="font-semibold">Lead</p>
            <p>${m.lead}</p>
        </div>

        <div class="mt-4">
            <p class="font-semibold">Conteúdo</p>
            <div class="prose max-w-none">${m.texto}</div>
        </div>

        ${m.imagem ? `
            <img src="/imprensa/materias/${m.id}/imagem"
                 class="rounded-xl mt-4 border">
        ` : ''}

        ${podeAprovar ? `
            <div class="flex gap-3 mt-6">
                <button onclick="aprovar(${m.id})"
                        class="bg-green-600 text-white px-4 py-2 rounded-lg">
                    Aprovar
                </button>

                <button onclick="rejeitar(${m.id})"
                        class="bg-red-600 text-white px-4 py-2 rounded-lg">
                    Rejeitar
                </button>
            </div>
        ` : ''}
    `;
}


function selectMateria(id) {
    selectedMateriaId = id;
    renderMateriaDetails(getMateriaById(id));
}

function filterMaterias() {
    const searchInput = document.getElementById('search-input');
    if (!searchInput) return;

    const search = searchInput.value.toLowerCase();

    const filtered = currentMaterias.filter(m =>
        m.titulo?.toLowerCase().includes(search) ||
        m.autor?.toLowerCase().includes(search)
    );

    renderLista('lista-todas', filtered);
}


document.addEventListener('DOMContentLoaded', () => {
    fetchMaterias();

    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', filterMaterias);
    }
});

async function carregarDetalhes(id) {
    try {
        const res = await fetch(`/imprensa/materias/${id}`, {
            credentials: 'include'
        });

        if (!res.ok) throw new Error('Erro ao carregar matéria');

        const materia = await res.json();
        renderMateriaDetails(materia);

    } catch (e) {
        console.error(e);
    }
}

function aprovar(id) {
    fetch(`/materias/${id}/aprovar`, {
        method: 'POST',
        credentials: 'include'
    }).then(() => location.reload());
}

function rejeitar(id) {
    const motivo = prompt('Motivo da rejeição:');
    if (!motivo) return;

    fetch(`/materias/${id}/rejeitar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `motivo=${encodeURIComponent(motivo)}`,
        credentials: 'include'
    }).then(() => location.reload());
}
