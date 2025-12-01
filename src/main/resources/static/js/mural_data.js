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
            posts: [],
            loading: false,
            posting: false,
            newMessage: '',
            postAsAnon: false,
            currentUser: null,
            currentUserEmail: null,
            error: null,
            isSecretary: false,
            emojis: ['❤️', '📢', '📌', '✅', '❌'],

            async init() {
                await this.loadCurrentUser();
                await this.loadPosts();
            },

            async loadCurrentUser() {
                try {
                    const res = await fetch(USER_ENDPOINT);
                    if (!res.ok) throw new Error();
                    const data = await res.json();
                    this.currentUser = data.nome ?? data.username ?? null;
                    this.currentUserEmail = data.email ?? data.username ?? null;
                    this.isSecretary = !!data.isSecretario;
                } catch {
                    this.currentUser = null;
                    this.currentUserEmail = null;
                    this.isSecretary = false;
                }
            },

            async loadPosts() {
                this.loading = true;
                try {
                    const url = this.currentUserEmail ? `${API_BASE}?usuario=${encodeURIComponent(this.currentUserEmail)}` : API_BASE;
                    const res = await fetch(url);
                    if (!res.ok) throw new Error('status ' + res.status);
                    const data = await res.json();
                    data.sort((a, b) => new Date(b.data) - new Date(a.data));
                    this.posts = (data || [])
                        .filter(p => p.status !== 'EXCLUIDO')
                        .map(p => ({
                            ...p,
                            _reacting: false,
                            _commentsOpen: false,
                            _loadingComments: false,
                            _newComment: '',
                            _postingComment: false,
                            _curtidasModalOpen: false,
                            _curtidasList: [],
                            _loadingCurtidas: false,
                            _curtindo: false,
                            _curtidaCount: 0,
                            _hasCurtido: false
                        }));

                    if (this.posts.length > 0) {
                        await Promise.all(this.posts.map(async post => {
                            try {
                                const r = await fetch(`${API_BASE}/${post.id}/curtidas`);
                                if (!r.ok) {
                                    post._curtidaCount = 0;
                                    post._hasCurtido = false;
                                    return;
                                }
                                const likes = await r.json();
                                post._curtidaCount = Array.isArray(likes) ? likes.length : 0;
                                const me = this.currentUserEmail || this.currentUser || null;
                                post._hasCurtido = me ? likes.some(u => (u.usuario || '').toLowerCase() === (me || '').toLowerCase()) : false;
                            } catch (e) {
                                post._curtidaCount = 0;
                                post._hasCurtido = false;
                            }
                        }));
                    }
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
                if (!this.isSecretary) {
                    alert('Apenas secretários podem excluir posts.');
                    return;
                }
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
                    } else if (res.status === 403) {
                        alert('Você não tem permissão para excluir este post.');
                    } else {
                        alert('Falha ao ocultar post (status ' + res.status + ')');
                    }
                } catch (err) {
                    alert('Erro ao ocultar post: ' + err.message);
                }
            },

            async addReaction(post, emoji) {
                if (!emoji || !post) return;
                if (post._reacting) return;
                post._reacting = true;
                const usuario = this.currentUserEmail || this.currentUser || 'anônimo';
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
                    const prev = post.myReaction || null;
                    if (res.status === 201) {
                        post.reactions = post.reactions || {};
                        post.reactions[emoji] = (post.reactions[emoji] || 0) + 1;
                        post.myReaction = emoji;
                        if (prev && prev !== emoji) {
                            post.reactions[prev] = Math.max(0, (post.reactions[prev] || 1) - 1);
                            if (post.reactions[prev] === 0) delete post.reactions[prev];
                        }
                    } else if (res.status === 200) {
                        if (prev && prev !== emoji) {
                            post.reactions[prev] = Math.max(0, (post.reactions[prev] || 1) - 1);
                            if (post.reactions[prev] === 0) delete post.reactions[prev];
                        }
                        post.reactions = post.reactions || {};
                        post.reactions[emoji] = (post.reactions[emoji] || 0) + 1;
                        post.myReaction = emoji;
                    } else if (res.status === 204) {
                        if (prev) {
                            post.reactions[prev] = Math.max(0, (post.reactions[prev] || 1) - 1);
                            if (post.reactions[prev] === 0) delete post.reactions[prev];
                        }
                        post.myReaction = null;
                    } else {
                        await this.loadPosts();
                    }
                } catch (err) {
                    alert('Erro ao reagir: ' + err.message);
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

            async toggleComments(post) {
                if (!post) return;
                post._commentsOpen = !post._commentsOpen;
                if (post._commentsOpen && (!post.comments || post.comments.length === 0)) {
                    await this.loadComments(post);
                }
            },

            async loadComments(post) {
                if (!post) return;
                post._loadingComments = true;
                try {
                    const url = `${API_BASE}/${post.id}/comments`;
                    const res = await fetch(url);
                    if (!res.ok) throw new Error('status ' + res.status);
                    const data = await res.json();
                    post.comments = (data || [])
                        .filter(c => (c.status || c.estado || null) !== 'EXCLUIDO')
                        .map(c => ({
                            ...c,
                            createdAt: c.createdAt || c.created_at,
                            usuarioNome: c.usuarioNome || c.usuario || c.nome || c.email,
                            status: c.status || c.estado || null
                        }));
                    post._newComment = post._newComment || '';
                } catch (err) {
                    post.comments = [];
                } finally {
                    post._loadingComments = false;
                }
            },

            async addComment(post) {
                if (!post || !post._newComment || !post._newComment.trim()) return;
                if (post._postingComment) return;
                post._postingComment = true;
                const usuario = this.currentUserEmail || this.currentUser || 'anônimo';
                const usuarioNome = this.currentUser || this.currentUserEmail || 'anônimo';
                const body = { usuario, mensagem: post._newComment.trim() };
                const { token, header } = readCsrf();
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers[header] = token;
                try {
                    const res = await fetch(`${API_BASE}/${post.id}/comments`, {
                        method: 'POST',
                        headers,
                        body: JSON.stringify(body)
                    });
                    if (res.status === 201) {
                        const json = await res.json();
                        const createdId = json.id;
                        const newComment = {
                            id: createdId,
                            postId: post.id,
                            usuario: usuario,
                            usuarioNome: usuarioNome,
                            mensagem: post._newComment.trim(),
                            createdAt: new Date().toISOString(),
                            status: null
                        };
                        post.comments = post.comments || [];
                        post.comments.push(newComment);
                        post._newComment = '';
                    } else {
                        alert('Falha ao enviar comentário (status ' + res.status + ')');
                    }
                } catch (err) {
                    alert('Erro ao comentar: ' + err.message);
                } finally {
                    post._postingComment = false;
                }
            },

            async deleteComment(post, commentId) {
                if (!confirm('Remover este comentário?')) return;
                const { token, header } = readCsrf();
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers[header] = token;
                try {
                    const res = await fetch(`${API_BASE}/${post.id}/comments/${commentId}/exclude`, {
                        method: 'PATCH',
                        headers
                    });
                    if (res.status === 204 || res.ok) {
                        post.comments = (post.comments || []).filter(c => c.id !== commentId);
                    } else if (res.status === 403) {
                        alert('Sem permissão para remover este comentário.');
                    } else {
                        alert('Falha ao remover (status ' + res.status + ')');
                    }
                } catch (err) {
                    alert('Erro ao remover comentário: ' + err.message);
                }
            },

            async toggleCurtida(post) {
                if (!post) return;
                if (post._curtindo) return;
                post._curtindo = true;
                const usuario = this.currentUserEmail || this.currentUser || 'anônimo';
                const usuarioNome = this.currentUser || this.currentUserEmail || null;
                const body = { usuario, usuarioNome };
                const { token, header } = readCsrf();
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers[header] = token;
                try {
                    const res = await fetch(`${API_BASE}/${post.id}/curtida`, {
                        method: 'POST',
                        headers,
                        body: JSON.stringify(body)
                    });
                    if (res.status === 201) {
                        post._curtidaCount = (post._curtidaCount || 0) + 1;
                        post._hasCurtido = true;
                    } else if (res.status === 204) {
                        post._curtidaCount = Math.max(0, (post._curtidaCount || 1) - 1);
                        post._hasCurtido = false;
                    } else {
                        const updated = await this.loadPosts();
                    }
                } catch (err) {
                    await this.loadPosts();
                } finally {
                    post._curtindo = false;
                }
            },

            openCurtidasModal: async function (post) {
                if (!post) return;
                post._loadingCurtidas = true;
                post._curtidasList = [];
                post._curtidasModalOpen = true;
                try {
                    const res = await fetch(`${API_BASE}/${post.id}/curtidas`);
                    if (!res.ok) throw new Error('status ' + res.status);
                    const data = await res.json();
                    post._curtidasList = (data || []).map(u => ({ usuario: u.usuario, usuarioNome: u.usuarioNome || u.usuario }));
                } catch (err) {
                    post._curtidasList = [];
                } finally {
                    post._loadingCurtidas = false;
                }
            },

            closeCurtidasModal: function (post) {
                if (!post) return;
                post._curtidasModalOpen = false;
            },

        };
    };
})();
