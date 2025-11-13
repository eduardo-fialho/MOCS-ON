function announcementsData() {
    return {
        avisos: [],
        currentPage: 1,
        itemsPerPage: 5,
        isLoading: false,
        isModalOpen: false,
        currentAviso: { titulo: '', mensagem: '', autor: '', data: '' },

        get paginatedAvisos() {
            const start = (this.currentPage - 1) * this.itemsPerPage;
            const end = start + this.itemsPerPage;
            return this.avisos.slice(start, end);
        },
        get totalPages() {
            return Math.ceil(this.avisos.length / this.itemsPerPage);
        },
        nextPage() {
            if (this.currentPage < this.totalPages) this.currentPage++;
        },
        prevPage() {
            if (this.currentPage > 1) this.currentPage--;
        },
        showAvisoModal(aviso) {
            this.currentAviso = aviso;
            this.isModalOpen = true;
        },
        closeAvisoModal() {
            this.isModalOpen = false;
            this.currentAviso = { titulo: '', mensagem: '', autor: '', data: '' };
        },
        formatTimeAgo(dateString) {
            try {
                const postDate = new Date(dateString);
                const now = new Date();
                const diffInSeconds = Math.floor((now - postDate) / 1000);
                if (diffInSeconds < 60) return `${diffInSeconds} segundos atrás`;
                const diffInMinutes = Math.floor(diffInSeconds / 60);
                if (diffInMinutes < 60) return `${diffInMinutes} minutos atrás`;
                const diffInHours = Math.floor(diffInMinutes / 60);
                if (diffInHours < 24) return `${diffInHours} horas atrás`;
                const diffInDays = Math.floor(diffInHours / 24);
                if (diffInDays < 7) return `${diffInDays} dias atrás`;
                return postDate.toLocaleDateString('pt-BR', { day: 'numeric', month: 'short', year: 'numeric' });
            } catch {
                return 'Data inválida';
            }
        },
        async loadAvisos() {
            this.isLoading = true;
            try {
                const response = await fetch(API_ENDPOINT);
                if (!response.ok) throw new Error('Erro ao carregar avisos.');
                const data = await response.json();
                data.sort((a, b) => new Date(b.data) - new Date(a.data));
                this.avisos = data;
                this.currentPage = 1;
            } catch (error) {
                console.error('Falha ao buscar avisos:', error);
                this.avisos = [];
            } finally {
                this.isLoading = false;
            }
        }
    };
}