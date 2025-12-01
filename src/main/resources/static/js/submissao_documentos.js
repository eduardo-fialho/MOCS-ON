const form = document.getElementById('submission-form');
const messageArea = document.getElementById('message-area');

const EXTENSOES_PERMITIDAS = ['pdf'];

async function getLoggedUser() {
    try {
        const response = await fetch('/user');
        if (!response.ok) throw new Error('Não foi possível obter informações do usuário');
        const data = await response.json();
        return data.nome;
    } catch (err) {
        console.error(err);
        return "Usuário Desconhecido";
    }
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const fileInput = document.getElementById('arquivo');
    const file = fileInput.files[0];

    if (!file) {
        showMessage('Por favor, selecione um arquivo.', 'red');
        return;
    }

    if (file.size > 10 * 1024 * 1024) {
        showMessage('O arquivo excede o limite de 10MB.', 'red');
        return;
    }

    const nomeArquivo = file.name;
    const extensao = nomeArquivo.split('.').pop().toLowerCase();
    if (!EXTENSOES_PERMITIDAS.includes(extensao)) {
        showMessage('Tipo de arquivo não permitido. Somente PDF é aceito.', 'red');
        return;
    }

    const nomeUsuario = await getLoggedUser();

    const formData = new FormData();
    formData.append("nome", document.getElementById("nome").value);
    formData.append("autor", nomeUsuario);
    formData.append("file", file);
    formData.append("avaliacao", "Recebido com sucesso!");

    try {
        const response = await fetch('/documentos', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            showMessage('Documento enviado com sucesso!', 'green');
            form.reset();
        } else {
            const text = await response.text();
            throw new Error(text || 'Erro no envio do documento');
        }
    } catch (err) {
        showMessage(err.message, 'red');
    }
});

function showMessage(text, color) {
    messageArea.style.display = 'block';
    if (color === 'red') {
        messageArea.className = 'mt-4 text-sm font-medium p-3 rounded-lg text-center bg-red-100 text-red-700';
    } else {
        messageArea.className = 'mt-4 text-sm font-medium p-3 rounded-lg text-center bg-green-100 text-green-700';
    }
    messageArea.textContent = text;
}