function user_data() {
    return {
        menuOpen: false,
        userName: 'Carregando...',
        userCargo: 'CSNU',
        isSecretario: false,
        userAvatar: DEFAULT_AVATAR_URL,
        defaultAvatar: DEFAULT_AVATAR_URL,

        init() {
            this.loadUserInfo();
        },

        toggleMenu() {
            this.menuOpen = !this.menuOpen;
        },

        async loadUserInfo() {
            try {
                const response = await fetch("/user", {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Não autenticado');
                }

                const user = await response.json();

                this.userName = user.nome ?? user.username ?? 'Usuário';
                this.userAvatar = user.avatar
                    ? `${USER_AVATAR_ENDPOINT}/${user.avatar}`
                    : this.defaultAvatar;

                this.isSecretario = user.isSecretario;

            } catch (e) {
                console.warn('Falha ao carregar usuário:', e);
                this.userName = 'Visitante';
                this.userAvatar = this.defaultAvatar;
                this.isSecretario = false;
            }
        }
    };
}
user_data();

let materias = [];
let materiasFiltradas = [];
let materiaSelecionada = null;
const MATERIA_DETAILS = document.getElementById('detalhes-materia');
MATERIA_DETAILS.style.display = "none";

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

function formatDateBR(dateString) {
    try {
        const d = new Date(dateString);

        if (isNaN(d.getTime())) throw new Error('Data inválida');

        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();

        return `${day}/${month}/${year}`;
    } catch (e) {
        return 'data inválida';
    }
}

document.addEventListener('DOMContentLoaded', carregarMaterias);

async function carregarMaterias() {
    const res = await fetch('/imprensa/materias');
    const dados = await res.json();

    materias = dados.filter(m => m.status === 'APROVADA');
    materiasFiltradas = [...materias];

    renderLista();
}

function renderLista() {
    const lista = document.getElementById('lista-materias');

    if (materiasFiltradas.length === 0) {
        lista.innerHTML = `
            <p class="text-slate-500 text-sm text-center">
                Nenhuma matéria encontrada
            </p>
        `;
        return;
    }

    lista.innerHTML = materiasFiltradas
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .map(m => `
            <article
                onclick="abrirMateria(${m.id})"
                class="cursor-pointer bg-mocs-blue text-white p-2 rounded-lg border hover:shadow transition"
            >
                <h3 class="font-semibold line-clamp-2">
                    ${m.titulo}
                </h3>

                <p class="text-xs mt-1">
                    ${timeAgo(m.createdAt)}
                </p>
            </article>
        `)
        .join('');
}

async function abrirMateria(id) {
    const res = await fetch(`/imprensa/materias/${id}`);
    const m = await res.json();

    materiaSelecionada = m;

    const d = document.getElementById('detalhes-materia');

    d.innerHTML = `
        <h2 class="text-4xl font-bold materia-title mb-3">${m.titulo}</h2>

        <div class="text-slate-600 mb-4">
            <p>${m.lead}</p>
        </div>

        <p class="text-sm text-slate-800 mb-4">
            Autor: <strong><span class="text-mocs-blue">${m.autor}</span></strong>
            <br> <b> ${formatDateBR(m.createdAt)} </b> | Há ${timeAgo(m.createdAt)}
        </p>

        ${m.imagem ? `
            <img
                src="/imprensa/materias/${m.id}/imagem"
                class="rounded-xl mb-4 border"
            >
        ` : ''}

        <div class="text-xl max-w-none">
            ${m.texto}
        </div>
    `;

    d.style.display = "block";
}

function aplicarFiltros() {
    const titulo = document.getElementById('filtro-titulo').value.toLowerCase();
    const data = document.getElementById('filtro-data').value;

    materiasFiltradas = materias.filter(m => {

        const matchTitulo =
            !titulo || m.titulo.toLowerCase().includes(titulo);

        const matchData =
            !data || m.createdAt.startsWith(data);

        return matchTitulo && matchData;
    });

    renderLista();
    MATERIA_DETAILS.style.display = "none";
}
