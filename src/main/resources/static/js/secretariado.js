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

(() => {
    const layer = document.getElementById('toast-layer');
    if (!layer) return;

    function showGlassToast(title, desc, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `glass-toast ${type}`;
        toast.innerHTML = `
                <div class="title">${title || 'Aviso'}</div>
                <div class="desc">${desc || ''}</div>
            `;
        const ripple = document.createElement('span');
        ripple.className = 'glass-ripple';
        ripple.style.left = '50%';
        ripple.style.top = '50%';
        toast.appendChild(ripple);
        layer.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('show'));
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 400);
        }, 2800);
    }
    window.showGlassToast = showGlassToast;
    window.toastSuccess = (title, desc) => showGlassToast(title || 'Tudo certo', desc || '', 'success');
    window.toastError = (title, desc) => showGlassToast(title || 'Algo deu errado', desc || '', 'error');

    window.confirmGlass = function (message, title = 'Confirmacao') {
        return new Promise((resolve) => {
            const backdrop = document.createElement('div');
            backdrop.className = 'glass-confirm-backdrop';
            const card = document.createElement('div');
            card.className = 'glass-confirm-card';
            card.innerHTML = `
                    <div class="relative z-10 space-y-3 text-slate-900">
                        <p class="text-xs uppercase tracking-[0.28em] text-slate-500">${title}</p>
                        <h3 class="text-lg font-semibold">Tem certeza?</h3>
                        <p class="text-sm text-slate-700">${message || ''}</p>
                        <div class="flex gap-2 pt-2">
                            <button id="gc-cancel" class="flex-1 px-4 py-2 rounded-full border border-white/60 text-slate-700 font-semibold hover:bg-white/40 transition">Cancelar</button>
                            <button id="gc-ok" class="flex-1 px-4 py-2 rounded-full bg-red-500 text-white font-semibold hover:bg-red-600 transition">Sim</button>
                        </div>
                    </div>
                `;
            backdrop.appendChild(card);
            document.body.appendChild(backdrop);
            requestAnimationFrame(() => card.classList.add('show'));
            const clean = (val) => {
                card.classList.remove('show');
                setTimeout(() => backdrop.remove(), 200);
                resolve(val);
            };
            backdrop.addEventListener('click', (e) => { if (e.target === backdrop) clean(false); });
            card.querySelector('#gc-cancel').addEventListener('click', () => clean(false));
            card.querySelector('#gc-ok').addEventListener('click', () => clean(true));
        });
    };

    const logoutLink = document.getElementById('logoutLink');
    if (logoutLink) {
        logoutLink.addEventListener('click', async (e) => {
            e.preventDefault();
            const ok = await window.confirmGlass('Deseja realmente sair da conta?', 'Logout');
            if (ok) window.location.href = logoutLink.getAttribute('href');
        });
    }

    const anuncioForm = document.getElementById('form-anuncio-modal');
    if (anuncioForm) {
        anuncioForm.addEventListener('submit', () => {
            toastSuccess('Anúncio enviado!', 'Seu anúncio foi postado com sucesso.');
        });
    }
})();