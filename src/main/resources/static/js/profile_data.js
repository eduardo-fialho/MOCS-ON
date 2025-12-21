function profileData() {
    return {
        menuOpen: false,
        academicOpen: false,
        userName: 'Carregando...',
        userCargo: 'CSNU',
        isSecretario: false,
        userAvatar: DEFAULT_AVATAR_URL,
        defaultAvatar: DEFAULT_AVATAR_URL,

        init() {
            return this.loadUserInfo();
        },

        toggleMenu() {
            this.menuOpen = !this.menuOpen;
        },

        openAcademic() {
            this.academicOpen = true;
        },

        closeAcademic() {
            this.academicOpen = false;
        },

        async loadUserInfo() {
            try {
                const response = await fetchWithTimeout(USER_ENDPOINT, { credentials: 'include' });

                if (!response.ok) throw new Error('Não autenticado');

                const user = await response.json();

                this.userName = user.nome ?? user.username ?? 'Usuário';
                this.userAvatar = resolveAvatarUrl(this.userName, this.defaultAvatar);
                this.isSecretario = user.isSecretario;
                syncAvatarTargets(this.userName, this.defaultAvatar);

            } catch (e) {
                console.warn('Falha ao carregar usuario:', e);
                this.userName = 'Visitante';
                this.userAvatar = this.defaultAvatar;
                this.isSecretario = false;
            }
        }
    };
}

document.addEventListener("DOMContentLoaded", async () => {
    const dropdownBtn = document.getElementById("user-dropdown-btn");
    const dropdown = document.getElementById("user-dropdown");

    if (dropdownBtn && dropdown) {
        dropdownBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            dropdown.classList.toggle("hidden");
        });

        document.addEventListener("click", () => {
            dropdown.classList.add("hidden");
        });
    }

    const userData = profileData();
    await userData.init();

    const userAvatar = document.getElementById("user-avatar");
    const userName = document.getElementById("user-name");
    const secretariadoLink = document.getElementById("secretariado-link");

    if (userAvatar) {
        userAvatar.src = userData.userAvatar;
    }
    if (userName) {
        userName.textContent = userData.userName;
    }
    if (userData.isSecretario && secretariadoLink) {
        secretariadoLink.style.display = "block";
    }
});

const USER_FETCH_TIMEOUT_MS = 4000;

function fetchWithTimeout(url, options = {}, timeoutMs = USER_FETCH_TIMEOUT_MS) {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), timeoutMs);
    const opts = { ...options, signal: controller.signal };
    return fetch(url, opts).finally(() => clearTimeout(id));
}
function resolveAvatarUrl(name, fallback) {
    if (!name || typeof USER_AVATAR_ENDPOINT === 'undefined') {
        return fallback;
    }
    const encoded = encodeURIComponent(String(name).trim());
    return `${USER_AVATAR_ENDPOINT}?name=${encoded}&ts=${Date.now()}`;
}

function syncAvatarTargets(name, fallback) {
    const targets = document.querySelectorAll('img[data-user-avatar]');
    if (!targets.length) {
        return;
    }
    const url = resolveAvatarUrl(name, fallback);
    targets.forEach(img => {
        img.src = url;
        const timer = setTimeout(() => {
            if (!img.complete || img.naturalWidth === 0) {
                img.src = fallback;
            }
        }, 3000);
        img.onload = () => clearTimeout(timer);
        img.onerror = () => {
            clearTimeout(timer);
            img.src = fallback;
        };
    });
}

document.addEventListener('DOMContentLoaded', async () => {
    if (typeof USER_ENDPOINT === 'undefined' || typeof USER_AVATAR_ENDPOINT === 'undefined') {
        return;
    }
    const targets = document.querySelectorAll('img[data-user-avatar]');
    if (!targets.length) {
        return;
    }
    try {
        const response = await fetchWithTimeout(USER_ENDPOINT, { credentials: 'include' });
        if (!response.ok) {
            throw new Error('Nao autenticado');
        }
        const user = await response.json();
        const name = user.nome ?? user.username ?? null;
        const fallback = typeof DEFAULT_AVATAR_URL !== 'undefined' ? DEFAULT_AVATAR_URL : '';
        syncAvatarTargets(name, fallback);
    } catch (e) {
        const fallback = typeof DEFAULT_AVATAR_URL !== 'undefined' ? DEFAULT_AVATAR_URL : '';
        targets.forEach(img => {
            img.src = fallback;
        });
    }
});
