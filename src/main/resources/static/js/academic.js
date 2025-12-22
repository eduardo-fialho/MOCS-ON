function normalizeText(value) {
    return (value || '')
        .toString()
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '');
}

function filterAcademicList() {
    var input = document.getElementById('academicSearch');
    var query = normalizeText(input ? input.value : '');
    var cards = document.querySelectorAll('[data-academic-card="true"]');
    cards.forEach(function (card) {
        var content = normalizeText(card.textContent || '');
        var visible = !query || content.indexOf(query) !== -1;
        card.style.display = visible ? '' : 'none';
    });
}