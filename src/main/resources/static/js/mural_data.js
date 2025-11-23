(function () {
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

    window.muralData = function () {
        return {
            // estado
            posts: [],
            loading: false,
            posting: false,
            newMessage: '',
            postAsAnon: false,
            currentUser: null,
            error: null,

            // lista de emojis centralizada
            emojis: ['❤️', '📢', '📌', '✅', '❌'],

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
                    // se currentUser existe, peça myReaction do backend
                    const url = this.currentUser ? `${API_BASE}?usuario=${encodeURIComponent(this.currentUser)}` : API_BASE;
                    const res = await fetch(url);
                    if (!res.ok) throw new Error('status ' + res.status);
                    const data = await res.json();
                    data.sort((a, b) => new Date(b.data) - new Date(a.data));

                    // inicializa flags locais para cada post (_reacting)
                    this.posts = data
                        .filter(p => p.status !== 'EXCLUIDO')
                        .map(p => ({ ...p, _reacting: false })); // cria campo local _reacting
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

            /**
             * Faz a ação de reagir ao post.
             * Recebe o objeto `post` (referência do array) e o emoji.
             * Atualiza o post localmente conforme o response status (201/200/204).
             */
            async addReaction(post, emoji) {
                if (!emoji || !post) return;
                // evita cliques múltiplos enquanto aguarda
                if (post._reacting) return;
                post._reacting = true;

                const usuario = this.currentUser || 'anônimo';
                const body = { usuario, emoji };
                const { token, header } = readCsrf();
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers[header] = token;

                try {
                    const res = await fetch(`${API_BASE}/${post.id}/reaction`, {
                        method: 'POST',
                        headers,
                        body: JSON.stringify(body)
                    });

                    // snapshot do prev (string ou null)
                    const prev = post.myReaction || null;

                    if (res.status === 201) {
                        // criada: incrementa contador do emoji e marca myReaction
                        post.reactions = post.reactions || {};
                        post.reactions[emoji] = (post.reactions[emoji] || 0) + 1;
                        post.myReaction = emoji;

                        // se havia prev diferente (improvável neste caso), decrementa
                        if (prev && prev !== emoji) {
                            post.reactions[prev] = Math.max(0, (post.reactions[prev] || 1) - 1);
                            if (post.reactions[prev] === 0) delete post.reactions[prev];
                        }
                    } else if (res.status === 200) {
                        // atualizada: decrementa prev e incrementa novo
                        if (prev && prev !== emoji) {
                            post.reactions[prev] = Math.max(0, (post.reactions[prev] || 1) - 1);
                            if (post.reactions[prev] === 0) delete post.reactions[prev];
                        }
                        post.reactions = post.reactions || {};
                        post.reactions[emoji] = (post.reactions[emoji] || 0) + 1;
                        post.myReaction = emoji;
                    } else if (res.status === 204) {
                        // removida: decrementa prev e limpa myReaction
                        if (prev) {
                            post.reactions[prev] = Math.max(0, (post.reactions[prev] || 1) - 1);
                            if (post.reactions[prev] === 0) delete post.reactions[prev];
                        }
                        post.myReaction = null;
                    } else {
                        // fallback: se der erro inesperado, recarrega para sincronizar
                        await this.loadPosts();
                    }
                } catch (err) {
                    alert('Erro ao reagir: ' + err.message);
                    // em caso de erro, recarrega para garantir consistência
                    await this.loadPosts();
                } finally {
                    post._reacting = false;
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