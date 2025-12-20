document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('guiaForm');
    const addLinkBtn = document.getElementById('addLinkBtn');
    const linksContainer = document.getElementById('linksContainer');
    const noLinksText = document.getElementById('noLinksText');
    const submitBtn = document.getElementById('submitBtn');
    const selectComite = document.getElementById('id_comite');
    const fileInput = document.getElementById('arquivo');

    // -----------------------------
    // Links dinâmicos
    // -----------------------------
    function atualizarTextoLinks() {
        noLinksText.style.display =
            linksContainer.children.length === 0 ? 'block' : 'none';
    }

    function criarCampoLink() {
        const wrapper = document.createElement('div');
        wrapper.className = 'flex gap-2 mb-2';

        const input = document.createElement('input');
        input.type = 'url';
        input.required = true;
        input.placeholder = 'URL completa';
        input.className = 'flex-1 px-3 py-2 border rounded-lg';

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.innerHTML = '<i class="fas fa-times"></i>';
        removeBtn.className = 'text-red-500';

        removeBtn.addEventListener('click', () => {
            wrapper.remove();
            atualizarTextoLinks();
        });

        wrapper.appendChild(input);
        wrapper.appendChild(removeBtn);
        linksContainer.appendChild(wrapper);

        atualizarTextoLinks();
    }

    addLinkBtn.addEventListener('click', criarCampoLink);

    fileInput.addEventListener('change', () => {
        const file = fileInput.files[0];
        if (!file) return;

        const isPdf =
            file.type === 'application/pdf' ||
            file.name.toLowerCase().endsWith('.pdf');

        if (!isPdf) {
            alert('Apenas arquivos PDF são permitidos.');
            fileInput.value = '';
        }
    });

    async function carregarComites() {
        try {
            const response = await fetch('/comite/informacoes');
            if (!response.ok) {
                throw new Error('Erro ao carregar comitês');
            }

            const comites = await response.json();

            comites.forEach(comite => {
                const option = document.createElement('option');
                option.value = comite.id;
                option.textContent = `${comite.nome} (${comite.sigla})`;
                selectComite.appendChild(option);
            });

        } catch (err) {
            console.error(err);
            alert('Não foi possível carregar os comitês.');
        }
    }

    carregarComites();

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const comiteId = selectComite.value;
        if (!comiteId) {
            alert('Selecione um comitê antes de enviar.');
            return;
        }

        if (fileInput.files.length > 0) {
            const file = fileInput.files[0];

            const isPdf =
                file.type === 'application/pdf' ||
                file.name.toLowerCase().endsWith('.pdf');

            if (!isPdf) {
                alert('Somente arquivos PDF podem ser enviados.');
                return;
            }
        }

        submitBtn.disabled = true;
        submitBtn.innerHTML =
            '<i class="fas fa-spinner fa-spin mr-2"></i> Enviando...';

        try {
            const formData = new FormData();
            formData.append('autor', document.getElementById('autor').value);
            formData.append('titulo', document.getElementById('titulo').value);
            formData.append('conteudo', document.getElementById('conteudo').value);
            formData.append('regras', document.getElementById('regras').value);
            formData.append('id_comite', comiteId);

            linksContainer
                .querySelectorAll('input[type="url"]')
                .forEach(input => {
                    formData.append('links', input.value);
                });

            if (fileInput.files.length > 0) {
                formData.append('arquivo', fileInput.files[0]);
            }

            const response = await fetch('/guia-estudos', {
                method: 'POST',
                body: formData
            });

            const text = await response.text();
            if (!response.ok) {
                throw new Error(text);
            }

            alert('Guia de estudo criado com sucesso!');
            window.location.href = '/guias_de_estudos.html';

        } catch (err) {
            console.error(err);
            alert(err.message || 'Erro ao enviar guia');

        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML =
                '<i class="fas fa-upload mr-2"></i> Submeter Guia';
        }
    });

});
