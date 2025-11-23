const form = document.getElementById('submission-form');
const messageArea = document.getElementById('message-area');

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fileInput = document.getElementById('arquivo');
    if (fileInput.files[0].size > 10 * 1024 * 1024) { // 10MB
        messageArea.style.display = 'block';
        messageArea.className = 'mt-4 text-sm font-medium p-3 rounded-lg text-center bg-red-100 text-red-700';
        messageArea.textContent = 'O arquivo excede o limite de 10MB.';
        return;
    }

    const formData = new FormData(form);

    try {
        const response = await fetch('/documentos', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            messageArea.style.display = 'block';
            messageArea.className = 'mt-4 text-sm font-medium p-3 rounded-lg text-center bg-green-100 text-green-700';
            messageArea.textContent = 'Documento enviado com sucesso!';
            form.reset();
        } else {
            throw new Error('Erro no envio do documento');
        }
    } catch (err) {
        messageArea.style.display = 'block';
        messageArea.className = 'mt-4 text-sm font-medium p-3 rounded-lg text-center bg-red-100 text-red-700';
        messageArea.textContent = err.message;
    }
});