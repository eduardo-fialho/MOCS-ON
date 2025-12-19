document.addEventListener("DOMContentLoaded", () => {

    const guiasList = document.getElementById("guias-list");
    const searchInput = document.getElementById("search-input");
    const btnArquivar = document.getElementById("btn-arquivar");
    const btnVisualizar = document.getElementById("btn-visualizar");
    const detalhesContainer = document.getElementById("detalhes-container");

    let guias = [];
    let usuarioIsSecretario = false;

    async function carregarUsuario() {
        try {
            const res = await fetch("/user");
            if (!res.ok) throw new Error("Erro ao obter informações do usuário");
            const user = await res.json();
            usuarioIsSecretario = user.isSecretario;
        } catch (err) {
            console.error(err);
        }
    }

    async function carregarGuias() {
        try {
            const res = await fetch("/guia-estudos");
            if (!res.ok) throw new Error("Erro ao carregar guias");
            guias = await res.json();
            renderizarGuias(guias);
        } catch (err) {
            console.error(err);
            guiasList.innerHTML = "<p class='text-red-600'>Não foi possível carregar os guias.</p>";
        }
    }

    function renderizarGuias(lista) {
        guiasList.innerHTML = "";
        if (lista.length === 0) {
            guiasList.innerHTML = "<p>Nenhum guia encontrado.</p>";
            detalhesContainer.classList.add("hidden");
            return;
        }
        lista.forEach(guia => {
            const div = document.createElement("div");
            div.className = "p-3 border rounded-lg hover:bg-slate-100 cursor-pointer transition";
            div.textContent = guia.titulo + " - " + guia.autor;
            div.addEventListener("click", () => mostrarDetalhes(guia));
            guiasList.appendChild(div);
        });
    }

    searchInput.addEventListener("input", () => {
        const filtro = searchInput.value.toLowerCase();
        const filtrados = guias.filter(g => g.titulo.toLowerCase().includes(filtro) || g.autor.toLowerCase().includes(filtro));
        renderizarGuias(filtrados);
    });

    async function mostrarDetalhes(guia) {
        detalhesContainer.classList.remove("hidden");
        document.getElementById("detalhe-titulo").textContent = guia.titulo;
        document.getElementById("detalhe-autor").textContent = guia.autor;

        try {
            const res = await fetch(`comite/informacoes/${guia.idComite}`);
            if (!res.ok) throw new Error("Erro ao buscar comitê");
            const comite = await res.json();
            document.getElementById("detalhe-comite").textContent = comite.nome || "-";
        } catch (err) {
            console.error(err);
            document.getElementById("detalhe-comite").textContent = "-";
        }

        btnVisualizar.onclick = () => {
            window.location.href = `/guia_estudo.html?id=${guia.id}`;
        };

        if (usuarioIsSecretario) {
            btnArquivar.style.display = "block";
            btnArquivar.onclick = async () => {
                if (!confirm("Deseja realmente arquivar este guia?")) return;
                try {
                    const res = await fetch(`/guia-estudos/${guia.id}/deletar`);
                    if (!res.ok) throw new Error("Erro ao arquivar guia");
                    alert("Guia arquivado com sucesso!");
                    carregarGuias();
                    detalhesContainer.classList.add("hidden");
                } catch (err) {
                    console.error(err);
                    alert("Não foi possível arquivar o guia.");
                }
            };
        } else {
            btnArquivar.style.display = "none";
        }
    }

    (async function init() {
        await carregarUsuario();
        await carregarGuias();
    })();

});
