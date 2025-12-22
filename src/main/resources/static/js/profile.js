function setupcâmeraControls(options) {
    const startBtn = document.getElementById(options.startBtnId);
    const captureBtn = document.getElementById(options.captureBtnId);
    const closeBtn = document.getElementById(options.closeBtnId);
    const video = document.getElementById(options.videoId);
    const canvas = document.getElementById(options.canvasId);
    const hiddenInput = document.getElementById(options.hiddenInputId);
    const form = document.getElementById(options.formId);
    if (!startBtn || !captureBtn || !closeBtn || !video || !canvas || !hiddenInput || !form) {
        return;
    }
    let mediaStream = null;

    async function startcâmera() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            alert('A câmera nao esta disponivel neste dispositivo.');
            return;
        }
        try {
            mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
            video.srcObject = mediaStream;
            video.classList.remove('hidden');
            captureBtn.disabled = false;
            closeBtn.disabled = false;
            captureBtn.classList.remove('opacity-60');
            closeBtn.classList.remove('opacity-60');
        } catch (err) {
            console.error('Erro ao acessar a câmera', err);
            alert('Nao foi possivel acessar a câmera.');
        }
    }

    function stopcâmera() {
        if (mediaStream) {
            mediaStream.getTracks().forEach(track => track.stop());
            mediaStream = null;
        }
        video.classList.add('hidden');
        captureBtn.disabled = true;
        closeBtn.disabled = true;
        captureBtn.classList.add('opacity-60');
        closeBtn.classList.add('opacity-60');
    }

    function capturePhoto() {
        if (!mediaStream) {
            alert('Abra a câmera primeiro.');
            return;
        }
        const trackSettings = mediaStream.getVideoTracks()[0].getSettings();
        canvas.width = trackSettings.width || 640;
        canvas.height = trackSettings.height || 480;
        const context = canvas.getContext('2d');
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        hiddenInput.value = canvas.toDataURL('image/png');
        stopcâmera();
        if (typeof options.beforeSubmit === 'function') {
            options.beforeSubmit();
        }
        form.submit();
    }

    startBtn.addEventListener('click', startcâmera);
    captureBtn.addEventListener('click', capturePhoto);
    closeBtn.addEventListener('click', stopcâmera);
    window.addEventListener('beforeunload', stopcâmera);
}

document.addEventListener('DOMContentLoaded', () => {
    function setupCameraControls(options) {
        const startBtn = document.getElementById(options.startBtnId);
        const captureBtn = document.getElementById(options.captureBtnId);
        const closeBtn = document.getElementById(options.closeBtnId);
        const video = document.getElementById(options.videoId);
        const canvas = document.getElementById(options.canvasId);
        const hiddenInput = document.getElementById(options.hiddenInputId);
        const form = document.getElementById(options.formId);

        if (!startBtn || !captureBtn || !closeBtn || !video || !canvas || !hiddenInput || !form) return;

        let mediaStream = null;

        async function startCamera() {
            try {
                mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
                video.srcObject = mediaStream;
                video.classList.remove('hidden');
                captureBtn.disabled = false;
                closeBtn.disabled = false;
            } catch {
                alert('N�o foi poss�vel acessar a c�mera.');
            }
        }

        function stopCamera() {
            if (mediaStream) {
                mediaStream.getTracks().forEach(t => t.stop());
                mediaStream = null;
            }
            video.classList.add('hidden');
            captureBtn.disabled = true;
            closeBtn.disabled = true;
        }

        function capturePhoto() {
            if (!mediaStream) return alert('Abra a c�mera primeiro.');
            canvas.width = video.videoWidth || 640;
            canvas.height = video.videoHeight || 480;
            canvas.getContext('2d').drawImage(video, 0, 0);
            hiddenInput.value = canvas.toDataURL('image/png');
            if (typeof options.beforeSubmit === 'function') options.beforeSubmit();
            form.submit();
            stopCamera();
        }

        startBtn.onclick = startCamera;
        captureBtn.onclick = capturePhoto;
        closeBtn.onclick = stopCamera;
        window.addEventListener('beforeunload', stopCamera);

    }
});

const grid = document.getElementById('userGalleryGrid');
const author = document.body.getAttribute('data-user-name');
const userType = (document.body.getAttribute('data-user-type') || '').toUpperCase();
const canModerate = userType === 'SECRETARIADO';
const modal = document.getElementById('galleryModal');
const modalImg = document.getElementById('galleryModalImg');
const modalCaption = document.getElementById('galleryModalCaption');
const modalMeta = document.getElementById('galleryModalMeta');
const modalClose = document.getElementById('galleryModalClose');
const likeBtn = document.getElementById('galleryLikeBtn');
const likeCount = document.getElementById('galleryLikeCount');
const commentsBox = document.getElementById('galleryComments');
const commentForm = document.getElementById('galleryCommentForm');
const commentInput = document.getElementById('galleryCommentInput');
const heartShower = document.getElementById('heartShower');
let currentPostId = null;

const closeGalleryModal = () => {
    if (!modal) return;
    modal.classList.remove('active');
    setTimeout(() => modal.classList.add('hidden'), 200);
};

const openGalleryModal = (src, caption, date, postId, reactions) => {
    if (!modal || !modalImg || !modalCaption || !modalMeta) return;
    modalImg.src = src;
    modalImg.alt = caption || 'Foto da galeria';
    modalCaption.textContent = caption || 'Publicação da galeria';
    modalMeta.textContent = date ? new Date(date).toLocaleString('pt-BR') : '';
    modal.classList.remove('hidden');
    requestAnimationFrame(() => modal.classList.add('active'));
    currentPostId = postId || null;
    const likes = reactions && reactions['❤️'] ? reactions['❤️'] : 0;
    if (likeCount) {
        likeCount.textContent = `${likes} curtida${likes === 1 ? '' : 's'}`;
    }
    if (commentsBox) {
        commentsBox.innerHTML = '<p class="text-xs text-gray-400">Carregando Comentários...</p>';
        loadComments(postId);
    }

    function initialsFromName(name) {
        if (!name) {
            return 'MO';
        }
        return name.split(/\s+/).slice(0, 2).map(part => part.charAt(0)).join('').toUpperCase();
    }

    function reactionTotal(reactions) {
        if (!reactions) {
            return 0;
        }
        return Object.values(reactions).reduce((sum, val) => sum + (Number(val) || 0), 0);
    }

    function escapeHtml(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function escapeAttr(value) {
        return escapeHtml(value).replace(/`/g, '&#96;');
    }

    function isOwner(name) {
        if (!name || !authorNormalized) {
            return false;
        }
        return name.trim().toLowerCase() === authorNormalized;
    }

    function setDeleteState(allowed) {
        if (!deleteBtn) {
            return;
        }
        deleteBtn.classList.toggle('hidden', !allowed);
    }

    function setLikeCount(count) {
        if (!likeCount) {
            return;
        }
        const safe = Math.max(0, Number(count) || 0);
        likeCount.textContent = `${safe} curtida${safe === 1 ? '' : 's'}`;
    }

    async function loadCurrentUserInfo() {
        try {
            const res = await fetch('/user', { credentials: 'same-origin' });
            if (!res.ok) throw new Error('status ' + res.status);
            const data = await res.json();
            currentUserEmail = data.email || data.username || null;
            currentUserName = data.nome || data.username || author || 'Usuario';
        } catch (err) {
            currentUserEmail = null;
            currentUserName = author || 'Usuario';
        }
    }

    function openModal(payload) {
        modalImg.src = payload.src;
        modalCaption.textContent = payload.caption || '';
        modalMeta.textContent = payload.date ? new Date(payload.date).toLocaleString('pt-BR') : '';
        if (modalLabel) modalLabel.textContent = payload.label || '';
        if (modalAuthor) modalAuthor.textContent = payload.authorDisplay || 'Usuario';
        if (modalInitials) modalInitials.textContent = initialsFromName(payload.authorDisplay);

        modal.classList.remove('hidden');
        requestAnimationFrame(() => modal.classList.add('active'));

        currentPostId = payload.postId;
        currentPostOwner = payload.authorRaw || payload.authorDisplay || '';
        const likes = reactionTotal(payload.reactions);
        setLikeCount(likes);

        setDeleteState(canModerate || isOwner(currentPostOwner));
        if (commentInput) commentInput.value = "";
        if (commentError) commentError.textContent = "";
        loadComments(payload.postId);
    }

    function parseGalleryPost(raw) {
        if (!raw || !raw.mensagem || !raw.mensagem.startsWith('PHOTO|')) {
            return null;
        }
        const parts = raw.mensagem.split('|');
        if (parts.length < 2) {
            return null;
        }
        const filename = parts[1];
        const caption = parts.slice(2).join('|').trim();
        const authorRaw = (raw.autorRaw || raw.autor || '').trim();
        const authorDisplay = (raw.autor || authorRaw || 'Usuario').trim();
        const label = raw.status === 'ANONIMO' ? 'Spotted' : 'Minha galeria';
        return {
            id: raw.id,
            caption,
            authorRaw,
            authorDisplay,
            label,
            publishedAt: raw.data,
            reactions: raw.reactions || {},
            url: `/profile/gallery/media/${filename}`
        };
    }

    function renderGallery(posts) {
        if (!grid) {
            return;
        }
        grid.innerHTML = posts.map(post => {
            const reactions = post.reactions || {};
            const likes = reactionTotal(reactions);
            const cardCaption = post.caption || post.authorDisplay || 'Sem legenda';
            return `
                        <button type="button"
                                class="dashboard-gallery-card relative rounded-2xl overflow-hidden shadow-sm group text-left cursor-pointer"
                                data-media-src="${escapeAttr(post.url)}"
                                data-caption="${escapeAttr(post.caption || '')}"
                                data-published-at="${escapeAttr(post.publishedAt || '')}"
                                data-post-id="${escapeAttr(post.id)}"
                                data-author="${escapeAttr(post.authorDisplay || '')}"
                                data-author-raw="${escapeAttr(post.authorRaw || '')}"
                                data-label="${escapeAttr(post.label || '')}"
                                data-reactions='${escapeAttr(JSON.stringify(reactions))}'>
                            <img class="w-full h-48 object-cover transition duration-300 group-hover:scale-105"
                                 src="${escapeAttr(post.url)}"
                                 alt="${escapeAttr(cardCaption)}">
                            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition duration-300 flex flex-col justify-end p-3 text-white">
                                <p class="text-sm font-semibold">${escapeHtml(cardCaption)}</p>
                                <span class="text-xs text-white/80">${escapeHtml(post.authorDisplay || '')}</span>
                            </div>
                            <div class="absolute top-3 left-3 bg-white/90 text-xs font-semibold px-2 py-1 rounded-full text-mocs-blue shadow">
                                ${escapeHtml(post.label || 'Minha galeria')}
                            </div>
                            <div class="absolute top-3 right-3 bg-black/60 text-xs text-white px-2 py-1 rounded-full flex items-center gap-1">
                                <i class="fas fa-heart text-red-400"></i>
                                <span>${likes}</span>
                            </div>
                        </button>
                    `;
        }).join('');
    }

    async function loadUserGallery() {
        if (!grid) {
            return;
        }
        grid.innerHTML = '<p class="col-span-full text-sm text-gray-400 text-center">Carregando sua galeria...</p>';
        try {
            const res = await fetch('/post/gallery');
            if (!res.ok) {
                throw new Error('status ' + res.status);
            }
            const data = await res.json();
            const posts = (data || [])
                .filter(post => post.status !== 'EXCLUIDO')
                .map(parseGalleryPost)
                .filter(post => post && (!authorNormalized || (post.authorRaw || '').toLowerCase() === authorNormalized));

            if (!posts.length) {
                grid.innerHTML = '<p class="col-span-full text-sm text-gray-400 text-center">Nenhuma foto publicada ainda.</p>';
                return;
            }
            renderGallery(posts);
        } catch (err) {
            grid.innerHTML = '<p class="col-span-full text-sm text-red-400 text-center">Nao foi possivel carregar sua galeria.</p>';
        }
    }
};

if (modalClose) modalClose.addEventListener('click', closeGalleryModal);
if (modal) {
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeGalleryModal();
    });
}
document.addEventListener('keyup', (e) => {
    if (e.key === 'Escape') closeGalleryModal();
});

function splashHearts(x, y) {
    if (!heartShower) return;
    heartShower.classList.remove('hidden');
    for (let i = 0; i < 10; i++) {
        const heart = document.createElement('div');
        heart.className = 'heart-pop';
        heart.textContent = '❤️';
        const offsetX = (Math.random() - 0.5) * 80;
        const offsetY = (Math.random() - 0.3) * 40;
        heart.style.left = `${x + offsetX}px`;
        heart.style.top = `${y + offsetY}px`;
        heartShower.appendChild(heart);
        setTimeout(() => {
            heart.remove();
            if (!heartShower.children.length) {
                heartShower.classList.add('hidden');
            }
        }, 1200);
    }
}

async function sendLike(postId) {

    likeBtn?.addEventListener('click', async () => {
        if (!currentPostId || !likeCount) return;
        const usuario = currentUserEmail || currentUserName || author || 'Usuario';
        const current = parseInt(likeCount.textContent, 10) || 0;
        try {
            const res = await fetch(`/post/${currentPostId}/reaction`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usuario, emoji: heartEmoji })
            });

            if (res.status === 201 || res.status === 200) {
                setLikeCount(current + 1);
            } else if (res.status === 204) {
                setLikeCount(current - 1);
            } else if (!res.ok) {
                alert('Falha ao registrar a curtida. Status: ' + res.status);
            }
        } catch (err) {
            alert('Nao foi possivel curtir agora.');
        }
    });

    deleteBtn?.addEventListener('click', async () => {
        if (!currentPostId) return;
        if (!(canModerate || isOwner(currentPostOwner))) {
            alert('Sem permissao para apagar.');
            return;
        }
        if (!confirm('Deseja remover esta foto da galeria?')) {
            return;
        }
        try {
            const res = await fetch(`/profile/gallery/${currentPostId}/delete`, { method: 'POST' });
            if (!res.ok && res.status !== 204) {
                throw new Error('status ' + res.status);
            }
            closeModal();
            await loadUserGallery();
        } catch (err) {
            alert('Nao foi possivel apagar a foto.');
        }
    });

    commentForm?.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!currentPostId) return;
        const text = (commentInput?.value || "").trim();
        if (!text) {
            if (commentError) commentError.textContent = 'Digite um comentario.';
            return;
        }
        if (commentError) commentError.textContent = "";
        const usuario = currentUserEmail || currentUserName || author || 'Usuario';
        try {
            const res = await fetch(`/post/${currentPostId}/comments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usuario, mensagem: text })
            });
            if (res.status !== 201) throw new Error('status ' + res.status);
            if (commentInput) commentInput.value = "";
            await loadComments(currentPostId);
        } catch (err) {
            if (commentError) commentError.textContent = 'Nao foi possivel salvar o comentario.';
        }
    });

}

if (likeBtn) {
    likeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const rect = likeBtn.getBoundingClientRect();
        splashHearts(rect.left + rect.width / 2, rect.top);
        sendLike(currentPostId);
    });
}

async function loadComments(postId) {
    if (!postId || !commentsBox) return;
    try {
        const res = await fetch(`/post/${postId}/comments`);
        if (!res.ok) {
            commentsBox.innerHTML = '<p class="text-xs text-gray-400">não foi possivel carregar os Comentários.</p>';
            return;
        }
        const data = await res.json();
        if (!Array.isArray(data) || data.length === 0) {
            commentsBox.innerHTML = '<p class="text-xs text-gray-400">Sem Comentários ainda.</p>';
            return;
        }
        commentsBox.innerHTML = '';
        data.forEach(c => {
            const item = document.createElement('div');
            item.className = 'rounded-lg bg-gray-50 border border-gray-100 px-3 py-2';
            const dateStr = c.createdAt ? new Date(c.createdAt).toLocaleString('pt-BR') : '';
            item.innerHTML = `<p class="text-sm font-semibold text-gray-800">${c.autor || 'Delegado'}</p><p class="text-sm text-gray-700">${c.mensagem || ''}</p><span class="text-[11px] text-gray-400">${dateStr}</span>`;
            commentsBox.appendChild(item);
        });
    } catch (err) {
        commentsBox.innerHTML = '<p class="text-xs text-red-400">Erro ao carregar Comentários.</p>';
    }
}

if (commentForm && commentInput) {
    commentForm.addEventListener('submit', async (evt) => {
        evt.preventDefault();
        const msg = commentInput.value.trim();
        if (!msg || !currentPostId) return;
        try {
            const res = await fetch(`/post/${currentPostId}/comments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ autor: author || 'Delegado', mensagem: msg })
            });
            if (res.ok) {
                commentInput.value = '';
                loadComments(currentPostId);
            }
        } catch (err) {
            console.warn('Erro ao comentar', err);
        }
    });
}

if (!grid) return;
grid.addEventListener('click', (evt) => {
    const deleteBtn = evt.target.closest('[data-delete-id]');
    if (deleteBtn) {
        evt.preventDefault();
        const postId = deleteBtn.dataset.deleteId;
        const card = deleteBtn.closest('[data-media-src]');
        if (postId) {
            const confirmed = window.confirm('Deseja excluir esta foto?');
            if (!confirmed) return;
            fetch(`/profile/gallery/${postId}/delete`, { method: 'POST' })
                .then(res => {
                    if (res.ok) {
                        if (card) card.remove();
                    } else if (res.status === 403) {
                        alert('Você não tem permissão para excluir esta foto.');
                    } else {
                        alert('não foi possivel excluir a foto.');
                    }
                })
                .catch(() => alert('não foi possivel excluir a foto.'));
        }
        return;
    }

    const target = evt.target;
    const card = target.closest('[data-media-src]');
    if (!card) return;
    let reactions = null;
    if (card.dataset.reactions) {
        try { reactions = JSON.parse(card.dataset.reactions); } catch (e) { reactions = null; }
    }
    openGalleryModal(card.dataset.mediaSrc, card.dataset.caption, card.dataset.publishedAt, card.dataset.postId, reactions);
    loadCurrentUserInfo();
    loadUserGallery();

    grid?.addEventListener('click', e => {
        const card = e.target.closest('[data-media-src]');
        if (!card) return;

        const reactions = card.dataset.reactions
            ? JSON.parse(card.dataset.reactions)
            : {};

        openModal({
            src: card.dataset.mediaSrc,
            caption: card.dataset.caption,
            date: card.dataset.publishedAt,
            postId: card.dataset.postId,
            reactions,
            authorDisplay: card.dataset.author,
            authorRaw: card.dataset.authorRaw,
            label: card.dataset.label
        });
    });

    const logoutBtn = document.getElementById('logoutButton');
    const logoutModal = document.getElementById('logoutModal');
    const cancelLogout = document.getElementById('cancelLogout');
    const confirmLogout = document.getElementById('confirmLogout');

    logoutBtn?.addEventListener('click', e => {
        e.preventDefault();
        logoutModal.classList.remove('hidden');
        requestAnimationFrame(() => logoutModal.classList.add('active'));
    });

    cancelLogout?.addEventListener('click', () => logoutModal.classList.add('hidden'));
    confirmLogout?.addEventListener('click', () => location.href = '/auth/logout');

});

if (!author) return;
fetch(`${window.location.origin}/post/gallery`)
    .then(res => res.ok ? res.json() : [])
    .then(data => {
        const mine = (data || []).filter(post => {
            const isDeleted = ((post.status || post.state || '').toUpperCase() === 'EXCLUIDO');
            if (isDeleted) return false;
            const rawAuthor = (post.autorRaw || post.autor || '').trim().toLowerCase();
            return rawAuthor === (author || '').trim().toLowerCase();
        });
        grid.innerHTML = '';
        if (!mine.length) {
            grid.innerHTML = '<p class="col-span-full text-sm text-gray-400 text-center">Voc? ainda n?o publicou nada na galeria.</p>';
            return;
        }

        mine.forEach(post => {
            const parts = (post.mensagem || '').split('|');
            const filename = parts[1];
            const caption = parts.slice(2).join('|') || author;
            const mediaSrc = `/profile/gallery/media/${filename}`;
            const publishedAt = post.data;
            const reactions = post.reactions || {};
            const rawAuthor = (post.autorRaw || post.autor || '').trim();
            const card = document.createElement('div');
            card.className = 'group relative rounded-2xl overflow-hidden shadow-sm border border-white/60 cursor-pointer';
            card.dataset.mediaSrc = mediaSrc;
            card.dataset.caption = caption;
            card.dataset.publishedAt = publishedAt || '';
            card.dataset.postId = post.id || '';
            card.dataset.reactions = JSON.stringify(reactions || {});
            const showDelete = canModerate || (author && rawAuthor && author.trim().toLowerCase() === rawAuthor.toLowerCase());
            card.innerHTML = `
                        <img src="${mediaSrc}" alt="${caption}" class="w-full h-40 object-cover">
                        <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition flex flex-col justify-end p-3 text-white">
                            <p class="text-sm font-semibold line-clamp-2">${caption}</p>
                            <span class="text-xs text-white/70">${new Date(post.data).toLocaleDateString('pt-BR')}</span>
                        </div>
                        ${showDelete ? '<button type="button" data-delete-id="' + (post.id || '') + '" class="absolute top-2 right-2 bg-white/90 text-red-600 rounded-full p-2 shadow hover:bg-white transition"><i class="fa-solid fa-trash"></i></button>' : ''}
                    `;
            card.addEventListener('click', () => openGalleryModal(mediaSrc, caption, publishedAt));
            grid.appendChild(card);
        });
    })
    .catch(() => {
        grid.innerHTML = '<p class="col-span-full text-sm text-red-400 text-center">não foi possivel carregar suas mídias.</p>';
    });

function setupCameraControls(options) {
    const startBtn = document.getElementById(options.startBtnId);
    const captureBtn = document.getElementById(options.captureBtnId);
    const closeBtn = document.getElementById(options.closeBtnId);
    const video = document.getElementById(options.videoId);
    const canvas = document.getElementById(options.canvasId);
    const hiddenInput = document.getElementById(options.hiddenInputId);
    const form = document.getElementById(options.formId);
    if (!startBtn || !captureBtn || !closeBtn || !video || !canvas || !hiddenInput || !form) return;
    let mediaStream = null;

    async function startCamera() {
        try {
            mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
            video.srcObject = mediaStream;
            video.classList.remove('hidden');
            captureBtn.disabled = false;
            closeBtn.disabled = false;
        } catch (err) {
            alert('Nao foi possivel acessar a camera.');
        }
    }
    function stopCamera() {
        if (mediaStream) {
            mediaStream.getTracks().forEach(t => t.stop());
            mediaStream = null;
        }
        video.classList.add('hidden');
        captureBtn.disabled = true;
        closeBtn.disabled = true;
    }
    function capturePhoto() {
        if (!mediaStream) { alert('Abra a camera primeiro.'); return; }
        canvas.width = video.videoWidth || 640;
        canvas.height = video.videoHeight || 480;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        hiddenInput.value = canvas.toDataURL('image/png');
        if (typeof options.beforeSubmit === 'function') options.beforeSubmit();
        form.submit();
        stopCamera();
    }
    startBtn.addEventListener('click', startCamera);
    captureBtn.addEventListener('click', capturePhoto);
    closeBtn.addEventListener('click', stopCamera);
    window.addEventListener('beforeunload', stopCamera);
}

document.addEventListener('DOMContentLoaded', () => {
    setupCameraControls({
        startBtnId: 'galleryStartcameraBtn',
        captureBtnId: 'galleryCaptureBtn',
        closeBtnId: 'galleryClosecameraBtn',
        videoId: 'gallerycameraPreview',
        canvasId: 'gallerycameraCanvas',
        hiddenInputId: 'galleryCapturedImageInput',
        formId: 'galleryCaptureForm',
        beforeSubmit: () => {
            document.getElementById('galleryCaptureCaptionInput').value = document.getElementById('galleryCaptureCaption').value;
            document.getElementById('galleryCaptureAnonInput').value = document.getElementById('galleryCaptureAnon').checked;
        }
    });

    const grid = document.getElementById('userGalleryGrid');
    const author = document.body.getAttribute('data-user-name');
    const userType = (document.body.getAttribute('data-user-type') || '').toUpperCase();
    const canModerate = userType === 'SECRETARIADO';
    const modal = document.getElementById('galleryModal');
    const modalImg = document.getElementById('galleryModalImg');
    const modalCaption = document.getElementById('galleryCaption');
    const modalMeta = document.getElementById('galleryMeta');
    const labelEl = document.getElementById('galleryLabel');
    const authorEl = document.getElementById('galleryAuthor');
    const initialsEl = document.getElementById('galleryInitials');
    const modalClose = document.getElementById('galleryModalClose');
    const likeBtn = document.getElementById('galleryLikeBtn');
    const likeCount = document.getElementById('galleryLikeCount');
    const commentsBox = document.getElementById('galleryComments');
    const commentForm = document.getElementById('galleryCommentForm');
    const commentInput = document.getElementById('galleryCommentInput');
    const commentError = document.getElementById('galleryCommentError');
    const deleteBtn = document.getElementById('galleryDeleteBtn');
    let currentPostId = null;
    let currentReactions = {};

    const closeGalleryModal = () => {
        if (!modal) return;
        modal.classList.add('hidden');
    };

    const openGalleryModal = (src, caption, date, postId, reactions, metaExtra = {}) => {
        if (!modal || !modalImg || !modalCaption || !modalMeta) return;
        modalImg.src = src;
        modalCaption.textContent = caption || '';
        modalMeta.textContent = date ? new Date(date).toLocaleString('pt-BR') : '';
        labelEl.textContent = metaExtra.label || '';
        authorEl.textContent = metaExtra.author || 'Delegado';
        initialsEl.textContent = metaExtra.initials || 'MO';
        modal.classList.remove('hidden');
        currentPostId = postId || null;
        currentReactions = reactions || {};
        const likes = currentReactions['❤'] || 0;
        if (likeCount) likeCount.textContent = `${likes}`;
        if (deleteBtn) deleteBtn.classList.toggle('hidden', !canModerate);
        if (commentsBox) {
            commentsBox.innerHTML = '<div class="text-center text-white/80 text-sm">Carregando comentarios...</div>';
            loadComments(postId);
        }
    };

    if (modalClose) modalClose.addEventListener('click', closeGalleryModal);
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal || e.target === document.getElementById('galleryModalOverlay')) closeGalleryModal();
        });
    }
    document.addEventListener('keyup', (e) => { if (e.key === 'Escape') closeGalleryModal(); });

    async function loadComments(postId) {
        if (!postId || !commentsBox) return;
        try {
            const res = await fetch(`/post/${postId}/comments`);
            if (!res.ok) throw new Error('Erro ao carregar comentarios');
            const list = await res.json();
            commentsBox.innerHTML = '';
            if (!list.length) {
                commentsBox.innerHTML = '<div class="text-center text-white/80 text-sm">Sem comentarios ainda.</div>';
                return;
            }
            list.forEach(c => {
                const div = document.createElement('div');
                div.className = 'rounded-xl bg-white/90 shadow px-3 py-2 border border-white/60';
                div.innerHTML = `<div class="flex justify-between text-xs text-gray-700"><span class="font-semibold text-gray-900">${c.autor}</span><span class="text-gray-500">${c.tempo}</span></div><p class="text-sm text-gray-900">${c.mensagem}</p>`;
                commentsBox.appendChild(div);
            });
        } catch (err) {
            commentsBox.innerHTML = '<div class="text-center text-red-300 text-xs">Erro ao carregar comentarios.</div>';
        }
    }

    if (likeBtn) {
        likeBtn.addEventListener('click', async () => {
            if (!currentPostId) return;
            try {
                await fetch(`/post/${currentPostId}/reaction`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ usuario: author || 'Usuario', emoji: '❤' })
                });
                currentReactions['❤'] = (currentReactions['❤'] || 0) + 1;
                if (likeCount) likeCount.textContent = `${currentReactions['❤']}`;
            } catch (err) {
                alert('Nao foi possivel registrar sua reacao.');
            }
        });
    }

    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
            if (!currentPostId || !canModerate) return;
            if (!confirm('Apagar esta foto?')) return;
            try {
                await fetch(`/profile/gallery/${currentPostId}/delete`, { method: 'POST' });
                const card = document.querySelector(`[data-post-id="${currentPostId}"]`);
                if (card) card.remove();
                closeGalleryModal();
            } catch (err) {
                alert('Nao foi possivel apagar a foto.');
            }
        });
    }

    if (commentForm) {
        commentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!currentPostId) return;
            const message = (commentInput.value || '').trim();
            if (!message) return;
            try {
                const res = await fetch(`/post/${currentPostId}/comments`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ autor: author || 'Usuario', mensagem: message })
                });
                if (!res.ok) throw new Error('Erro ao enviar comentario');
                commentInput.value = '';
                loadComments(currentPostId);
            } catch (err) {
                if (commentError) commentError.textContent = 'Falha ao enviar comentario.';
            }
        });
    }

    if (grid) {
        grid.querySelectorAll('[data-post-id]').forEach(card => {
            card.addEventListener('click', () => {
                const mediaSrc = card.dataset.mediaSrc;
                const caption = card.dataset.caption || '';
                const publishedAt = card.dataset.publishedAt || '';
                const postId = card.dataset.postId;
                const authorName = card.dataset.author || 'Delegado';
                const label = card.dataset.label || '';
                const initials = (authorName || 'MO').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
                const reactions = { '❤': parseInt(card.dataset.likes || '0', 10) || 0 };
                openGalleryModal(mediaSrc, caption, publishedAt, postId, reactions, { author: authorName, label, initials });
            });
        });
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const trigger = document.getElementById('logoutButton');
    const modal = document.getElementById('logoutModal');
    const cancelBtn = document.getElementById('cancelLogout');
    const confirmBtn = document.getElementById('confirmLogout');
    if (!trigger || !modal || !cancelBtn || !confirmBtn) return;

    const open = () => {
        modal.classList.remove('hidden');
        requestAnimationFrame(() => modal.classList.add('active'));
    };
    const close = () => {
        modal.classList.remove('active');
        setTimeout(() => modal.classList.add('hidden'), 180);
    };

    trigger.addEventListener('click', (e) => {
        e.preventDefault();
        open();
    });
    cancelBtn.addEventListener('click', (e) => { e.preventDefault(); close(); });
    confirmBtn.addEventListener('click', (e) => {
        e.preventDefault();
        const target = confirmBtn.getAttribute('href') || '/auth/logout';
        close();
        setTimeout(() => { window.location.href = target; }, 150);
    });
    modal.addEventListener('click', (e) => { if (e.target === modal) close(); });
    document.addEventListener('keyup', (e) => { if (e.key === 'Escape') close(); });
});