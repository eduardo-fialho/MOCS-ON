document.addEventListener('alpine:init', () => {
    Alpine.data('galeriaPreview', () => ({

        searchQuery: '',
        searchLoading: false,
        searchError: null,
        searchResults: [],
        fallbackPosts: [],

        filters: ['Todos', 'Fotos', 'Vídeos'],
        activeFilter: 'Todos',

        feedLoading: false,
        feedError: null,
        filteredMedia: [],

        modalOpen: false,
        selectedMedia: null,
        modalPortrait: false,

        likeLoading: false,

        comments: [],
        commentLoading: false,
        commentError: null,
        commentText: '',

        init() {
            console.log('Galeria inicializada');
            this.loadFeed();
        },

        handleSearchInput() {
            if (this.searchQuery.trim().length < 2) return;
        },

        loadFeed() {
            this.feedLoading = true;
            this.filteredMedia = [];
            this.feedLoading = false;
        },

        openModal(media) {
            this.selectedMedia = media;
            this.modalOpen = true;
        },

        closeModal() {
            this.modalOpen = false;
            this.selectedMedia = null;
            this.comments = [];
        },

        toggleLike() {
            if (!this.selectedMedia) return;
            this.selectedMedia.liked = !this.selectedMedia.liked;
            this.selectedMedia.likes += this.selectedMedia.liked ? 1 : -1;
        },

        submitComment() {
            if (!this.commentText.trim()) return;
            this.comments.push({
                id: Date.now(),
                autor: 'Você',
                mensagem: this.commentText,
                tempo: 'agora'
            });
            this.commentText = '';
        }
    }));
});


document.addEventListener('DOMContentLoaded', () => {
    const grid = document.getElementById('dashboardGalleryGrid');
    const modal = document.getElementById('dashboardGalleryModal');
    const modalImg = document.getElementById('dashboardGalleryImg');
    const modalCaption = document.getElementById('dashboardGalleryCaption');
    const modalMeta = document.getElementById('dashboardGalleryMeta');

    if (!grid || !modal || !modalImg) return;

    const closeModal = () => modal.classList.add('hidden');

    modal.querySelectorAll('[data-gallery-dismiss]').forEach(btn => {
        btn.addEventListener('click', closeModal);
    });

    grid.addEventListener('click', (evt) => {
        const card = evt.target.closest('.dashboard-gallery-card');
        if (!card) return;
        const img = card.querySelector('img');
        const captionEl = card.querySelector('p');
        const metaEl = card.querySelector('span');

        modalImg.src = img ? img.src : '';
        modalImg.alt = img ? img.alt : 'Foto da galeria';
        if (modalCaption) modalCaption.textContent = captionEl ? captionEl.textContent : '';
        if (modalMeta) modalMeta.textContent = metaEl ? metaEl.textContent : '';

        modal.classList.remove('hidden');
    });
});
