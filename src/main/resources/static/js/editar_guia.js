document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('editarGuiaForm');
    const addLinkBtn = document.getElementById('addLinkBtn');
    const linksContainer = document.getElementById('linksContainer');
    const noLinksText = document.getElementById('noLinksText');
    const submitBtn = document.getElementById('submitBtn');
    const selectComite = document.getElementById('id_comite');
    const arquivoAtual = document.getElementById('arquivoAtual');
    const fileInput = document.getElementById('arquivo');

    const guiaId = new URLSearchParams(window.location.search).get('id');
    document.getElementById('guiaId').value = guiaId;

    function atualizarTextoLinks() {
        noLinksText.style.display =
            linksContainer.children.length === 0 ? 'block' : 'none';
    }

    function criarCampoLink(valor = '') {
        const wrapper = document.createElement('div');
        wrapper.className = 'flex gap-2 mb-2';

        const input = document.createElement('input');
        input.type = 'url';
        input.required = true;
        input.placeholder = 'URL completa';
        input.className = 'flex-1 px-3 py-2 border rounded-lg';
        input.value = valor;

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

    addLinkBtn.addEventListener('click', () => criarCampoLink());

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
            const res = await fetch('/comite/informacoes');
            if (!res.ok) throw new Error();
            const comites = await res.json();

            comites.forEach(c => {
                const option = document.createElement('option');
                option.value = c.id;
                option.textContent = `${c.nome} (${c.sigla})`;
                selectComite.appendChild(option);
            });
        } catch {
            alert('Não foi possível carregar os comitês.');
        }
    }

    async function carregarGuia() {
        try {
            const res = await fetch(`/guia-estudos/${guiaId}`);
            if (!res.ok) throw new Error();
            const guia = await res.json();

            document.getElementById('titulo').value = guia.titulo;
            document.getElementById('conteudo').value = guia.conteudo;
            document.getElementById('regras').value = guia.regras;
            selectComite.value = guia.idComite;

            if (guia.arquivo) {
                arquivoAtual.textContent =
                    'PDF já anexado. Você pode substituí-lo selecionando outro arquivo.';
            }

            if (guia.links?.length) {
                guia.links.forEach(l => criarCampoLink(l.link));
            }

        } catch {
            alert('Não foi possível carregar o guia.');
        }
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!selectComite.value) {
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

            formData.append('autor', 'Secretariado');
            formData.append('titulo', document.getElementById('titulo').value);
            formData.append('conteudo', document.getElementById('conteudo').value);
            formData.append('regras', document.getElementById('regras').value);
            formData.append('id_comite', selectComite.value);

            linksContainer
                .querySelectorAll('input[type="url"]')
                .forEach(i => formData.append('links', i.value));

            if (fileInput.files.length > 0) {
                formData.append('arquivo', fileInput.files[0]);
            }

            const res = await fetch(`/guia-estudos/${guiaId}/atualizar`, {
                method: 'POST',
                body: formData
            });

            const text = await res.text();
            if (!res.ok) throw new Error(text);

            alert('Guia atualizado com sucesso!');
            window.location.href = '/guias_de_estudos.html';

        } catch (err) {
            alert(err.message || 'Erro ao atualizar guia');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML =
                '<i class="fas fa-save mr-2"></i> Salvar Alterações';
        }
    });

    (async function init() {
        await carregarComites();
        await carregarGuia();
    })();
});