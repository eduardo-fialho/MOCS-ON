(function(){
    const API_BASE = POST_API_BASE;

    function readCsrf() {
        const token = document.querySelector('meta[name="_csrf"]')?.content || null;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        return { token, header };
    }

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

    window.muralData = function() {
        return {
            posts: [],
            loading: false,
            posting: false,
            newMessage: '',
            postAsAnon: false,
            currentUser: null,
            error: null,

            async init() {
                await this.loadCurrentUser();
                await this.loadPosts();
            },

            async loadCurrentUser() {
                try {
                    const res = await fetch(USER_ENDPOINT);
                    if (!res.ok) {
                        this.currentUser = null;
                        return;
                    }
                    const data = await res.json();
                    this.currentUser = data.nome || null;
                } catch (err) {
                    this.currentUser = null;
                }
            },

            async loadPosts() {
                this.loading = true;
                try {
                    const res = await fetch(API_BASE);
                    if (!res.ok) throw new Error('status ' + res.status);
                    const data = await res.json();
                    data.sort((a,b) => new Date(b.data) - new Date(a.data));
                    
                    this.posts = data.filter(p => p.status !== 'EXCLUIDO');
                } catch (err) {
                    this.posts = [];
                    this.error = 'Erro ao carregar posts';
                } finally {
                    this.loading = false;
                }
            },

            async createPost() {
                if (!this.newMessage || !this.newMessage.trim()) {
                    alert('Digite uma mensagem para postar.');
                    return;
                }
                this.posting = true;
                const body = {
                    autor: this.postAsAnon ? (this.currentUser || '') : (this.currentUser || 'Delegado'),
                    mensagem: this.newMessage.trim(),
                    status: this.postAsAnon ? 'ANONIMO' : 'PUBLICO'
                };

                const { token, header } = readCsrf();
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers[header] = token;

                try {
                    const res = await fetch(API_BASE, {
                        method: 'POST',
                        headers,
                        body: JSON.stringify(body)
                    });
                    if (res.status === 201) {
                        this.newMessage = '';
                        this.postAsAnon = false;
                        await this.loadPosts();
                    } else {
                        alert('Falha ao postar (status ' + res.status + ')');
                    }
                } catch (err) {
                    alert('Erro ao postar: ' + err.message);
                } finally {
                    this.posting = false;
                }
            },

            
            async deletePost(postId) {
                if (!confirm('O post não será mais exibido a outros usuarios')) return;
                const { token, header } = readCsrf();
                const headers = {};
                if (token) headers[header] = token;
                try {
                    const res = await fetch(`${API_BASE}/${postId}/exclude`, { method: 'PATCH', headers });
                    if (res.ok || res.status === 204) {
                        await this.loadPosts();
                    } else if (res.status === 404) {
                        alert('Post não encontrado (já removido?).');
                    } else {
                        alert('Falha ao ocultar post (status ' + res.status + ')');
                    }
                } catch (err) {
                    alert('Erro ao ocultar post: ' + err.message);
                }
            },

            async addReaction(postId, emoji) {
                if (!emoji) return;
                const usuario = this.currentUser || 'anônimo';
                const body = { usuario, emoji };
                const { token, header } = readCsrf();
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers[header] = token;

                try {
                    const res = await fetch(`${API_BASE}/${postId}/reaction`, {
                        method: 'POST',
                        headers,
                        body: JSON.stringify(body)
                    });
                    if (res.status === 201) {
                        await this.loadPosts();
                    } else if (res.status === 409) {
                      
                    } else {
                        throw new Error('status ' + res.status);
                    }
                } catch (err) {
                    alert('Erro ao reagir: ' + err.message);
                }
            },

            reactionCount(post, emoji) {
                if (!post || !post.reactions) return 0;
                return post.reactions[emoji] || 0;
            },

            timeAgo,
        };
    };
})();