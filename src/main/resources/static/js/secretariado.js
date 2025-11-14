const APP_CONTEXT_PATH = (() => {
    const path = window.location.pathname || '';
    const base = path.replace(/\/[^/]*$/, '');
    if (!base || base === '/' || base === path) return '';
    return base;
})();
const API_BASE_URL = `${window.location.origin}${APP_CONTEXT_PATH}`;
const AVISO_ENDPOINT = `${API_BASE_URL}/aviso`;

document.addEventListener("DOMContentLoaded", () => {
    const usuarioNome = document.body.getAttribute("data-usuario-nome");
    const usuarioTipo = document.body.getAttribute("data-usuario-tipo");

    const modal = document.getElementById("modal-anuncio");
    const abrirModalBtn = document.getElementById("abrir-modal-anuncio");
    const fecharModalBtn = document.getElementById("fechar-modal-btn");
    const fecharModalArea = document.getElementById("fechar-modal");
    const formAnuncio = document.getElementById("form-anuncio-modal");

    abrirModalBtn.addEventListener("click", () => modal.classList.remove("hidden"));
    fecharModalBtn.addEventListener("click", () => modal.classList.add("hidden"));
    fecharModalArea.addEventListener("click", () => modal.classList.add("hidden"));

    document.getElementById("botao-usuario").addEventListener("click", () => {
        document.getElementById("menu-usuario").classList.toggle("hidden");
    });

    function tempoDecorrido(timestamp) {
        const agora = new Date();
        const postData = new Date(timestamp);
        const diffSegundos = Math.floor((agora - postData) / 1000);
        if (diffSegundos < 0) return "agora";

        const intervalos = [
            { label: "ano", segundos: 31536000 },
            { label: "mês", segundos: 2592000 },
            { label: "dia", segundos: 86400 },
            { label: "hora", segundos: 3600 },
            { label: "minuto", segundos: 60 },
            { label: "segundo", segundos: 1 }
        ];

        for (let i = 0; i < intervalos.length; i++) {
            const interval = Math.floor(diffSegundos / intervalos[i].segundos);
            if (interval >= 1) return `${interval} ${intervalos[i].label}${interval > 1 ? 's' : ''} atrás`;
        }
        return "agora";
    }

    function atualizarTempos() {
        const elementos = document.querySelectorAll("[data-post]");
        elementos.forEach(el => el.textContent = tempoDecorrido(el.getAttribute("data-post")));
    }

    setInterval(atualizarTempos, 60000);
    atualizarTempos();

    formAnuncio.addEventListener("submit", async (e) => {
        e.preventDefault();

        const titulo = document.getElementById("anuncio-titulo-modal").value.trim();
        const mensagem = document.getElementById("anuncio-mensagem-modal").value.trim();
        if (!titulo || !mensagem) return alert("Preencha título e mensagem.");

        const botao = formAnuncio.querySelector("button");
        botao.disabled = true;
        botao.innerHTML = '<i class="fas fa-spinner animate-spin mr-2"></i> Publicando...';

        try {
            const response = await fetch(AVISO_ENDPOINT, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ autor: usuarioNome, titulo, mensagem })
            });

            if (response.status === 201) {
                const postData = new Date().toISOString();
                const lista = document.getElementById("lista-anuncios");
                const div = document.createElement("div");
                div.className = "bg-white p-4 rounded-xl shadow-md mb-4";
                div.innerHTML = `
                    <h3 class="font-bold text-mocs-blue text-lg">${titulo}</h3>
                    <p class="text-gray-700 mt-1">${mensagem}</p>
                    <span class="text-gray-500 text-sm" data-post="${postData}">agora</span>
                `;
                lista.prepend(div);
                formAnuncio.reset();
                modal.classList.add("hidden");
                atualizarTempos();
            } else {
                alert(`Falha ao postar anúncio. Status: ${response.status}`);
            }
        } catch (err) {
            alert(`Erro ao conectar com a API: ${err}`);
        } finally {
            botao.disabled = false;
            botao.innerHTML = '<i class="fas fa-paper-plane mr-2"></i> Publicar';
        }
    });
});