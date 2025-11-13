function profileData() {
    return {
        userName: 'Carregando...',
        isSecretario: false,

        async loadUserInfo() {
            try {
                const response = await fetch(USER_ENDPOINT); 

                if (!response.ok) {
                    this.userName = 'Erro ao carregar';
                    return;
                }

                const data = await response.json();
                
                this.userName = data.nome || 'Delegado';
                this.isSecretario = data.isSecretario || false;
            } catch (error) {
                console.error('Falha ao buscar info do usuário:', error);
                this.userName = 'Delegado (Offline)';
            }
        }
    };
}