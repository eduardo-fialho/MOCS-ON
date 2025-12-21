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

async function renderMateriaDetails(m) {
    const c = document.getElementById('details-view-container');
    if (!c || !m) return;

    const usuario = await getUsuarioLogado();
    console.log(usuario);
    
    const podeAprovar =
        m.status === 'PENDENTE' &&
        usuario.isSecretario 
        /* &&  m.autor !== usuario.email */
        ;
    
    const podeEditar = usuario.email == m.autor;

    c.innerHTML = `
        <h3 class="text-xl font-bold">${m.titulo}</h3>
        <div class="mt-4">
            <p>${m.lead}</p>
        </div>

        <p class="text-sm text-slate-500">
            Autor: <span class="text-mocs-blue"><strong>${m.autor}</strong></span>
        </p>

        <p class="text-sm text-slate-500">
            Criado em: ${timeAgo(m.createdAt)}
        </p>

        ${m.reviewedAt && m.status == "APROVADA"? `
            <p class="text-sm text-slate-500">
                Revisado por: <span class="text-mocs-blue"><strong>${m.revisor}</strong></span><br>
                Há: ${timeAgo(m.reviewedAt)}
            </p>
        ` : ''}

        ${m.imagem ? `
            <img src="/imprensa/materias/${m.id}/imagem"
                 class="rounded-xl mt-4 border">
        ` : ''
        }

        <div class="mt-4">
            <p class="font-semibold">Conteúdo</p>
            <div class="prose max-w-none">${m.texto}</div>
        </div>

        <h3 class="text-xl font-bold mb-4">Opções:</h3>
        <div class="flex flex-wrap gap-3 mt-4">
            ${podeAprovar ? `
                <button onclick="aprovar(${m.id})"
                        class="btn bg-green-600 text-white px-5 py-2 rounded-lg font-medium 
           transition-all duration-200
           hover:opacity-90 
           min-w-[110px] text-center">
                    Aprovar
                </button>

                <button onclick="rejeitar(${m.id})"
                        class="btn bg-red-600 text-white px-5 py-2 rounded-lg font-medium 
           transition-all duration-200
           hover:opacity-90 
           min-w-[110px] text-center">
                    Rejeitar
                </button>
            ` : ''}

            ${podeEditar ? `
                <button onclick="editar(${m.id})"
                        class="btn bg-orange-600 text-white px-5 py-2 rounded-lg font-medium 
           transition-all duration-200
           hover:opacity-90 
           min-w-[110px] text-center">
                    Editar
                </button>

                <button onclick="arquivar(${m.id})"
                        class="btn bg-orange-500
                        text-white px-5 py-2 rounded-lg font-medium 
           transition-all duration-200
           hover:opacity-90 
           min-w-[110px] text-center">
                    Arquivar
                </button>
            ` : ''}
        </div>
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

function arquivar(id) {
    fetch(`/materias/${id}/arquivar`, {
        method: 'POST',
        credentials: 'include'
    }).then(() => location.reload());
}

function editar(id) {
    window.location.href = `/materias/${id}/editar`;
}
