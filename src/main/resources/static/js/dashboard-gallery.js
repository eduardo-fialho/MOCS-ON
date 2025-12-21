function galeriaPreview() {
    return {
        filters: ['Meu comite', 'Secretariado', 'Global'],
        activeFilter: 'Meu comite',
        searchQuery: '',
        searchResults: [],
        searchLoading: false,
        searchError: null,
        searchDebounce: null,
        fallbackPosts: [],
        posts: [],
        media: [],
        feedLoading: true,
        feedError: null,
        currentUser: null,
        currentUserEmail: null,
        isSecretary: false,
        defaultAvatar: DEFAULT_AVATAR_URL,
        heartEmoji: '\u2764\uFE0F',
        modalOpen: false,
        selectedMedia: null,
        modalPortrait: false,
        likeLoading: false,
        comments: [],
        commentLoading: false,
        commentText: '',
        commentError: null,
        floatingHearts: [],
        applyLikesDelta(postId, delta) {
            const pick = list => Array.isArray(list) ? list.find(entry => entry.id === postId) : null;
            const target = pick(this.media) || pick(this.posts);
            if (target) {
                target.likes = Math.max(0, (target.likes || 0) + delta);
            }
            if (this.selectedMedia && this.selectedMedia.id === postId) {
                this.selectedMedia.likes = Math.max(0, (this.selectedMedia.likes || 0) + delta);
            }
        },
        decrementLikes(postId, delta = 1) {
            this.applyLikesDelta(postId, -delta);
        },
        init() {
            this.loadCurrentUser();
            this.loadFeed();
        },
        async loadCurrentUser() {
            try {
                const res = await fetch(USER_ENDPOINT);
                if (!res.ok) throw new Error('status ' + res.status);
                const data = await res.json();
                this.currentUser = data && data.nome ? data.nome : 'Delegado';
                this.currentUserEmail = data && (data.email || data.username) ? (data.email || data.username) : null;
                this.isSecretary = !!data.isSecretario;
            } catch (err) {
                console.error('Erro ao buscar usuário para a galeria:', err);
                this.currentUser = 'Delegado';
                this.currentUserEmail = null;
                this.isSecretary = false;
            }
        },
        async loadFeed() {
            this.feedLoading = true;
            this.feedError = null;
            try {
                const res = await fetch(`${POST_API_BASE}/gallery`);
                if (!res.ok) throw new Error('status ' + res.status);
                const data = await res.json();
                
                const visible = data
                    .filter(post => post.status !== 'EXCLUIDO')
                    .sort((a, b) => new Date(b.data) - new Date(a.data));
                const parsed = visible
                    .map((raw, index) => this.enrichPost(raw, index))
                    .filter(item => item !== null);
                this.posts = parsed;
                this.media = parsed;
                
            } catch (error) {
                console.error('Erro ao carregar galeria:', error);
                this.posts = [];
                this.media = [];
                this.feedError = 'No foi possíel carregar a galeria agora.';
            } finally {
                this.feedLoading = false;
            }
        },
        enrichPost(raw, index) {
            if (!raw || !raw.mensagem) {
                return null;
            }
            const message = raw.mensagem.trim();
            if (!message.startsWith('PHOTO|')) {
                return null;
            }
            const author = (raw.autor || 'Delegado').trim();
            const parts = message.split('|');
            if (parts.length < 2) {
                return null;
            }
            const filename = parts[1];
            const caption = parts.slice(2).join('|').trim();
            const segment = raw.autorTipo && /secret/i.test(raw.autorTipo) ? 'Secretariado' : 'Meu comite';
            const tag = raw.status === 'ANONIMO' ? '#Spotted' : `#${segment.replace(' ', '')}`;
            const initials = author.split(/\s+/).slice(0, 2).map(part => part.charAt(0)).join('').toUpperCase() || 'DL';
            const reactions = raw.reactions || {};
            const likes = this.getHeartCount(reactions);
            return {
                id: raw.id || index,
                author,
                initials,
                segment,
                tag,
                time: this.formatRelativeTime(raw.data),
                excerpt: this.summarize(caption || ''),
                rawMessage: caption || '',
                url: `/profile/gallery/media/${filename}`,
                caption: caption || '',
                meta: this.formatRelativeTime(raw.data),
                label: raw.comiteSigla && raw.comiteNome
                    ? `${raw.comiteSigla} - ${raw.comiteNome}`
                    : (raw.comiteSigla || raw.comiteNome || (segment === 'Secretariado' ? 'Secretariado' : 'Sem comite')),
                likes,
                avatar: DEFAULT_AVATAR_URL
            };
        },
        summarize(text, limit = 140) {
            if (!text) return '';
            return text.length > limit ? `${text.slice(0, limit - 3)}...` : text;
        },
        formatRelativeTime(value) {
            if (!value) return "agora mesmo";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return "agora mesmo";
            const diffSeconds = Math.max(1, Math.floor((Date.now() - date.getTime()) / 1000));
            if (diffSeconds < 60) return `ha ${diffSeconds}s`;
            const minutes = Math.floor(diffSeconds / 60);
            if (minutes < 60) return `ha ${minutes} min`;
            const hours = Math.floor(minutes / 60);
            if (hours < 24) return `ha ${hours} h`;
            const days = Math.floor(hours / 24);
            if (days < 7) return `ha ${days} d`;
            return date.toLocaleDateString("pt-BR", { day: "2-digit", month: "short" });
        },
        handleSearchInput() {
            clearTimeout(this.searchDebounce);
            this.searchError = null;
            if (!this.searchQuery || this.searchQuery.trim().length < 2) {
                this.searchResults = [];
                this.fallbackPosts = [];
                return;
            }
            this.searchDebounce = setTimeout(() => this.performSearch(), 300);
        },
        async performSearch() {
            const query = (this.searchQuery || '').trim();
            if (query.length < 2) {
                this.searchResults = [];
                this.fallbackPosts = [];
                return;
            }
            this.searchLoading = true;
            this.searchError = null;
            try {
                const res = await fetch(`${API_BASE_URL}/user/search?q=${encodeURIComponent(query)}`);
                if (!res.ok) throw new Error('status ' + res.status);
                const payload = await res.json();
                this.searchResults = payload.map(user => ({
                    ...user,
                    tipo: this.formatRoleLabel(user.tipo)
                }));
                if (!this.searchResults.length) {
                    this.buildFallbackPosts(query);
                } else {
                    this.fallbackPosts = [];
                }
            } catch (error) {
                console.error('Erro na busca:', error);
                this.searchError = 'Não foi possível buscar agora.';
                this.fallbackPosts = [];
            } finally {
                this.searchLoading = false;
            }
        },
        buildFallbackPosts(query) {
            const normalized = query.toLowerCase();
            this.fallbackPosts = this.posts
                .filter(post => post.author.toLowerCase().includes(normalized) || post.rawMessage.toLowerCase().includes(normalized))
                .slice(0, 3);
        },
        spawnHeart() {
            const heart = {
                id: Date.now() + Math.random(),
                left: 35 + Math.random() * 30
            };
            this.floatingHearts.push(heart);
            setTimeout(() => {
                this.floatingHearts = this.floatingHearts.filter(item => item.id !== heart.id);
            }, 1100);
        },
        incrementLikes(postId, delta = 1) {
            this.applyLikesDelta(postId, delta);
        },
        getHeartCount(reactions) {
            if (!reactions) return 0;
            const primary = this.heartEmoji;
            const fallback = '\u2764';
            let total = reactions[primary] || 0;
            if (fallback !== primary) {
                total += reactions[fallback] || 0;
            }
            return total;
        },
        openModal(media) {
            this.selectedMedia = { ...media, liked: media.liked || false };
            this.modalPortrait = !!media.isPortrait;
            this.commentText = '';
            this.commentError = null;
            this.modalOpen = true;
            this.floatingHearts = [];
            this.loadComments(media.id);
        },
        closeModal() {
            this.modalOpen = false;
            this.selectedMedia = null;
            this.comments = [];
            this.floatingHearts = [];
        },
        async loadComments(postId) {
            this.commentLoading = true;
            this.commentError = null;
            try {
                const res = await fetch(`${POST_API_BASE}/${postId}/comments`);
                if (!res.ok) throw new Error('status ' + res.status);
                const data = await res.json();
                this.comments = (data || [])
                    .filter(item => (item.status || item.estado || null) !== 'EXCLUIDO')
                    .map(item => ({
                        ...item,
                        autor: item.usuarioNome || item.usuario || item.autor || 'Usuario',
                        tempo: this.formatRelativeTime(item.createdAt || item.data || item.created_at)
                    }));
            } catch (err) {
                console.error('Erro ao buscar comentários:', err);
                this.commentError = 'Não foi possível carregar os comentários.';
                this.comments = [];
            } finally {
                this.commentLoading = false;
            }
        },
        async submitComment() {
            if (!this.selectedMedia || !this.commentText.trim()) {
                this.commentError = 'Escreva um comentário primeiro.';
                return;
            }
            this.commentError = null;
            try {
                const res = await fetch(`${POST_API_BASE}/${this.selectedMedia.id}/comments`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        usuario: this.currentUserEmail || this.currentUser || 'Delegado',
                        mensagem: this.commentText.trim()
                    })
                });
                if (!res.ok) throw new Error('status ' + res.status);
                this.commentText = '';
                await this.loadComments(this.selectedMedia.id);
            } catch (err) {
                console.error('Erro ao comentar:', err);
                this.commentError = 'Falha ao salvar seu comentário.';
            }
        },
        async toggleLike() {
            if (!this.selectedMedia || this.likeLoading) return;
            if (!this.selectedMedia.id) {
                alert('Não foi possível identificar o post para curtir.');
                return;
            }
            this.likeLoading = true;
            const liked = !!this.selectedMedia.liked;
            const { token, header } = (typeof readCsrf === 'function') ? readCsrf() : { token: null, header: 'X-CSRF-TOKEN' };
            const headers = { 'Content-Type': 'application/json' };
            if (token) headers[header] = token;
            const body = JSON.stringify({
                usuario: this.currentUserEmail || this.currentUser || 'Delegado',
                emoji: this.heartEmoji
            });
            const url = `${POST_API_BASE}/${this.selectedMedia.id}/reaction`;
            try {
                const res = await fetch(url, {
                    method: liked ? 'DELETE' : 'POST',
                    headers,
                    credentials: 'same-origin',
                    body
                });
            
                if (liked) {
                    if (res.ok) {
                        this.decrementLikes(this.selectedMedia.id, 1);
                        this.selectedMedia.liked = false;
                    } else {
                        console.error('Erro ao remover reação:', res.status, await res.text());
                        alert('Não foi possível remover sua reação. Status: ' + res.status);
                    }
                } else {
                 
                    if (res.ok) {
                        this.incrementLikes(this.selectedMedia.id, 1);
                        this.selectedMedia.liked = true;
                        this.spawnHeart();
                    } else if (res.status === 409) {
                        
                        this.selectedMedia.liked = true;
                    } else {
                        console.error('Erro ao reagir:', res.status, await res.text());
                        alert('Não foi possível registrar sua reação. Status: ' + res.status);
                    }
                }
            } catch (err) {
                console.error('Erro ao reagir na galeria:', err);
                alert('Erro ao reagir: ' + err.message);
            } finally {
                this.likeLoading = false;
            }
        },
        async deleteSelectedMedia() {
            if (!this.selectedMedia || !this.selectedMedia.id) {
                return;
            }
            if (!this.isSecretary) {
                alert('Apenas secretariado pode apagar fotos.');
                return;
            }
            if (!confirm('Deseja remover esta foto da galeria?')) {
                return;
            }
            try {
                const res = await fetch(`/profile/gallery/${this.selectedMedia.id}/delete`, {
                    method: 'POST',
                    credentials: 'same-origin'
                });
                if (!res.ok && res.status !== 204) {
                    throw new Error('status ' + res.status);
                }
                await this.loadFeed();
                this.closeModal();
            } catch (err) {
                console.error('Erro ao excluir foto:', err);
                alert('Nao foi possivel apagar a foto.');
            }
        },
        get filteredMedia() {
            if (this.activeFilter === 'Global') return this.media;
            return this.media.filter(item => item.segment === this.activeFilter);
        },
        formatRoleLabel(tipo) {
            if (!tipo) return 'Delegado';
            const cleaned = tipo.replace('ROLE_', '');
            return cleaned.charAt(0) + cleaned.slice(1).toLowerCase();
        },
        formatTime(value) {
            return value ? this.formatRelativeTime(value) : 'sem registro';
        }
    };
}

document.addEventListener('DOMContentLoaded', () => {
    const grid = document.getElementById('dashboardGalleryGrid');
    if (!grid) return;

    const modal = document.getElementById('dashboardGalleryModal');
    const modalImg = document.getElementById('dashboardGalleryImg');
    const modalCaption = document.getElementById('dashboardGalleryCaption');
    const modalMeta = document.getElementById('dashboardGalleryMeta');

    const closeModal = () => modal?.classList.add('hidden');

    modal?.querySelectorAll('[data-gallery-dismiss]').forEach(btn => {
        btn.addEventListener('click', closeModal);
    });

    grid.addEventListener('click', (evt) => {
        const card = evt.target.closest('.dashboard-gallery-card');
        if (!card || !modal || !modalImg) return;

        const img = card.querySelector('img');
        const captionEl = card.querySelector('p');
        const metaEl = card.querySelector('span');

        modalImg.src = img?.src || '';
        modalImg.alt = img?.alt || 'Foto da galeria';
        if (modalCaption) modalCaption.textContent = captionEl?.textContent || '';
        if (modalMeta) modalMeta.textContent = metaEl?.textContent || '';

        modal.classList.remove('hidden');
    });
});


