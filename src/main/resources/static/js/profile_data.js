function profileData() {
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
                const response = await fetch(USER_ENDPOINT, {
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
