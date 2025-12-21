const APP_CONTEXT_PATH = (() => {
    const path = window.location.pathname || '';
    const base = path.replace(/\/[^/]*$/, '');
    if (!base || base === '/' || base === path) return '';
    return base;
})();
const API_BASE_URL = `${window.location.origin}${APP_CONTEXT_PATH}`;
const AVISO_ENDPOINT = `${API_BASE_URL}/aviso`;
const USER_AVATAR_ENDPOINT = `${API_BASE_URL}/user/avatar`;

document.addEventListener("DOMContentLoaded", () => {
    const usuarioNome = document.body.getAttribute("data-usuario-nome");
    const usuarioTipo = document.body.getAttribute("data-usuario-tipo");
    const avatarImg = document.querySelector("#usuario-info img");
    const defaultAvatar = "https://placehold.co/40x40/205395/ffffff?text=S";

    const modal = document.getElementById("modal-anuncio");
    const fecharModalBtn = document.getElementById("fechar-modal-btn");
    const fecharModalArea = document.getElementById("fechar-modal");
    const formAnuncio = document.getElementById("form-anuncio-modal");
    const anunciosCountLabel = document.getElementById("anuncios-count");
    let anunciosCount = 0;

    const modalTriggers = document.querySelectorAll("[data-open-anuncio]");
    modalTriggers.forEach(trigger => {
        trigger.addEventListener("click", (event) => {
            event.preventDefault();
            modal.classList.remove("hidden");
        });
    });
    fecharModalBtn.addEventListener("click", () => modal.classList.add("hidden"));
    fecharModalArea.addEventListener("click", () => modal.classList.add("hidden"));

    const botaoUsuario = document.getElementById("botao-usuario");
    const menuUsuario = document.getElementById("menu-usuario");
    if (botaoUsuario && menuUsuario) {
        botaoUsuario.addEventListener("click", () => {
            menuUsuario.classList.toggle("hidden");
        });
    }

    async function carregarAvatar() {
        if (!avatarImg || !usuarioNome) {
            return;
        }
        try {
            const encoded = encodeURIComponent(usuarioNome.trim());
            const url = `${USER_AVATAR_ENDPOINT}?name=${encoded}&ts=${Date.now()}`;
            const response = await fetch(url);
            if (!response.ok) {
                avatarImg.src = defaultAvatar;
                return;
            }
            avatarImg.src = url;
        } catch (err) {
            console.warn("Falha ao carregar avatar do secretariado", err);
            avatarImg.src = defaultAvatar;
        }
    }

    carregarAvatar();

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

    carregarAnunciosExistentes();

    function atualizarTextoAnuncios() {
        if (!anunciosCountLabel) return;
        if (anunciosCount <= 0) {
            anunciosCountLabel.textContent = "Nenhum anúncio postado ainda.";
        } else {
            anunciosCountLabel.textContent = `${anunciosCount} anúncio${anunciosCount > 1 ? 's' : ''} postado${anunciosCount > 1 ? 's' : ''}.`;
        }
    }

    async function carregarAnunciosExistentes() {
        if (!anunciosCountLabel) return;
        try {
            const response = await fetch(AVISO_ENDPOINT);
            if (!response.ok) {
                return;
            }
            const data = await response.json();
            if (Array.isArray(data)) {
                anunciosCount = data.length;
                atualizarTextoAnuncios();
            }
        } catch (err) {
            console.warn("Não foi possível carregar anúncios existentes.", err);
            atualizarTextoAnuncios();
        }
    }

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
                anunciosCount += 1;
                atualizarTextoAnuncios();
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

    const galleryGrid = document.getElementById("dashboardGalleryGrid");
    const galleryModal = document.getElementById("dashboardGalleryModal");
    const galleryImg = document.getElementById("dashboardGalleryImg");
    const galleryCaption = document.getElementById("dashboardGalleryCaption");
    const galleryMeta = document.getElementById("dashboardGalleryMeta");

    const closeGalleryModal = () => {
        if (!galleryModal) return;
        galleryModal.classList.add("hidden");
    };

    if (galleryModal) {
        galleryModal.querySelectorAll("[data-gallery-dismiss]").forEach(btn => {
            btn.addEventListener("click", closeGalleryModal);
        });
    }

    if (galleryGrid) {
        galleryGrid.addEventListener("click", (evt) => {
            const card = evt.target.closest(".dashboard-gallery-card");
            if (!card || !galleryModal || !galleryImg) return;
            const imgEl = card.querySelector("img");
            const captionEl = card.querySelector("p");
            const metaEl = card.querySelector("span");
            galleryImg.src = imgEl ? imgEl.src : "";
            galleryImg.alt = imgEl ? imgEl.alt : "Foto da galeria";
            if (galleryCaption) galleryCaption.textContent = captionEl ? captionEl.textContent : "";
            if (galleryMeta) galleryMeta.textContent = metaEl ? metaEl.textContent : "";
            galleryModal.classList.remove("hidden");
        });
    }
});
