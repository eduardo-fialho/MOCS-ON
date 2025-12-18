function validarImagem(input) {
    const file = input.files[0];
    if (!file) return;

    if (file.type !== 'image/png') {
        alert('Apenas imagens PNG são permitidas.');
        input.value = '';
    }
}
