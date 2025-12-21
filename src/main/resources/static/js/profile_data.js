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
            this.loadUserInfo();
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
                if (typeof USER_ENDPOINT === 'undefined' || typeof USER_AVATAR_ENDPOINT === 'undefined') {
                    throw new Error('Endpoints indisponiveis');
                }
                const response = await fetch(USER_ENDPOINT, {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Nao autenticado');
                }

                const user = await response.json();

                this.userName = user.nome ?? user.username ?? 'Usuario';
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
        img.onerror = () => { img.src = fallback; };
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
        const response = await fetch(USER_ENDPOINT, { credentials: 'include' });
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
