// Modal de visualização da galeria no dashboard
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
