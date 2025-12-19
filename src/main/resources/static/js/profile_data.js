function profileData() {
    return {
        menuOpen: false,
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

        async loadUserInfo() {
            try {
                const response = await fetch(USER_ENDPOINT, { credentials: 'include' });

                if (!response.ok) throw new Error('Não autenticado');

                const user = await response.json();
                console.log(user);

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

document.addEventListener("DOMContentLoaded", async () => {
    const dropdownBtn = document.getElementById("user-dropdown-btn");
    const dropdown = document.getElementById("user-dropdown");

    dropdownBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        dropdown.classList.toggle("hidden");
    });

    document.addEventListener("click", () => {
        dropdown.classList.add("hidden");
    });

    const userData = profileData();
    await userData.init();

    document.getElementById("user-avatar").src = userData.userAvatar;
    document.getElementById("user-name").textContent = userData.userName;

    if (userData.isSecretario) {
        document.getElementById("secretariado-link").style.display = "block";
    }
});