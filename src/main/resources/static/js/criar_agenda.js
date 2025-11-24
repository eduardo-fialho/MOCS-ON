const botaoCriarEvento = document.getElementById("botao-abrir-form-criar-evento");
const formCriarEvento = document.getElementById("form-criar-evento");
const botaoCancelarCriar = document.getElementById("botao-cancelar-form-criar");

const botaoEditarEventos = document.getElementById("botao-abrir-editar-eventos");
const formEditarEventos = document.getElementById("form-editar-eventos");
const listaEditar = document.getElementById("lista-editar-eventos");
const botaoCancelarEdicao = document.getElementById("botao-cancelar-editar");

// Criar Evento
document.getElementById("form-criar-evento").addEventListener("submit", async function (e) {
    e.preventDefault();

    const dados = {
        titulo: document.getElementById("titulo").value.trim(),
        descricao: document.getElementById("descricao").value.trim(),
        data_evento: document.getElementById("data_evento").value,
        hora_evento: document.getElementById("hora_evento").value
    };

    if (!dados.titulo) {
        alert("Título é obrigatório.");
        return;
    }

    try {
        const resposta = await fetch("/agenda/criar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (resposta.ok) {
            alert("Evento criado com sucesso!");
            formCriarEvento.classList.add("hidden");
            botaoCriarEvento.classList.remove("hidden");
            botaoEditarEventos.classList.remove("hidden");
            document.getElementById("titulo").value = "";
            document.getElementById("descricao").value = "";
            document.getElementById("data_evento").value = "";
            document.getElementById("hora_evento").value = "";
        } else {
            const text = await resposta.text();
            alert("Erro ao criar evento: " + text);
        }
    } catch (err) {
        console.error(err);
        alert("Erro de rede ao criar evento.");
    }
});

botaoCriarEvento?.addEventListener("click", () => {
    formCriarEvento.classList.remove("hidden");
    botaoCriarEvento.classList.add("hidden");

    formEditarEventos.classList.add("hidden");
    botaoEditarEventos.classList.add("hidden");
});

botaoCancelarCriar?.addEventListener("click", () => {
    formCriarEvento.classList.add("hidden");
    botaoCriarEvento.classList.remove("hidden");
    botaoEditarEventos.classList.remove("hidden");
});

document.addEventListener("DOMContentLoaded", () => {
    const inputData = document.getElementById("data_evento");
    if (inputData) {
        const hoje = new Date();
        const ano = hoje.getFullYear();
        const mes = String(hoje.getMonth() + 1).padStart(2, "0");
        const dia = String(hoje.getDate()).padStart(2, "0");

        const dataMinima = `${ano}-${mes}-${dia}`;
        inputData.min = dataMinima;

        inputData.addEventListener("change", () => {
            if (inputData.value < dataMinima) {
                alert("Não é permitido criar eventos em datas passadas.");
                inputData.value = "";
            }
        });
    }
});

// Editar eventos
botaoEditarEventos?.addEventListener("click", () => {
    formEditarEventos.classList.toggle("hidden");

    formCriarEvento.classList.add("hidden");
    botaoCriarEvento.classList.add("hidden");
    botaoEditarEventos.classList.add("hidden");

    carregarEventosParaEdicao();
});

botaoCancelarEdicao?.addEventListener("click", () => {
    formEditarEventos.classList.add("hidden");
    botaoCriarEvento.classList.remove("hidden");
    botaoEditarEventos.classList.remove("hidden");
});

async function carregarEventosParaEdicao() {
    listaEditar.innerHTML = "<p class='text-gray-500'>Carregando...</p>";

    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;

    try {
        const resposta = await fetch(`/agenda/eventos?ano=${ano}&mes=${mes}`);

        if (!resposta.ok) {
            listaEditar.innerHTML = "<p class='text-red-500'>Erro ao buscar eventos.</p>";
            return;
        }

        const eventos = await resposta.json();

        if (!eventos || eventos.length === 0) {
            listaEditar.innerHTML = "<p class='text-gray-500'>Nenhum evento encontrado.</p>";
            return;
        }

        listaEditar.innerHTML = "";

        eventos.forEach(ev => {
            const bloco = document.createElement("div");
            bloco.className = "border p-4 rounded-md shadow-sm bg-gray-50";

            bloco.innerHTML = `
            <div class="mb-2">
                <label class="font-semibold text-gray-600">Título</label>
                <input type="text" value="${ev.titulo || ''}" class="input-titulo w-full px-3 py-2 border rounded-md">
            </div>

            <div class="mb-2">
                <label class="font-semibold text-gray-600">Descrição</label>
                <textarea class="input-descricao w-full px-3 py-2 border rounded-md">${ev.descricao || ''}</textarea>
            </div>

            <div class="grid grid-cols-2 gap-3 mb-2">
                <div>
                    <label class="font-semibold text-gray-600">Data</label>
                    <input type="date" value="${ev.data_evento || ''}" class="input-data w-full px-3 py-2 border rounded-md">
                </div>
                <div>
                    <label class="font-semibold text-gray-600">Hora</label>
                    <input type="time" value="${ev.hora_evento || ''}" class="input-hora w-full px-3 py-2 border rounded-md">
                </div>
            </div>

            <div class="flex gap-2 mt-2">
                <button type="button" class="botao-salvar-alteracoes bg-blue-600 text-white px-3 py-2 rounded-md w-full">Salvar Alterações</button>
                <button type="button" class="botao-excluir-evento bg-red-600 text-white px-3 py-2 rounded-md w-32">Excluir</button>
            </div>
            `;

            listaEditar.appendChild(bloco);

            bloco.querySelector(".botao-salvar-alteracoes").addEventListener("click", async () => {
                const titulo = bloco.querySelector(".input-titulo").value.trim();
                const descricao = bloco.querySelector(".input-descricao").value.trim();
                const data_evento = bloco.querySelector(".input-data").value;
                const hora_evento = bloco.querySelector(".input-hora").value;

                if (!titulo) {
                    alert("Título é obrigatório.");
                    return;
                }

                const dados = { titulo, descricao, data_evento, hora_evento };

                const respostaEd = await fetch(`/agenda/editar/${ev.id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(dados)
                });

                if (respostaEd.ok) {
                    alert("Evento atualizado!");
                    carregarEventosParaEdicao();
                } else {
                    alert("Erro ao atualizar evento.");
                }
            });

            bloco.querySelector(".botao-excluir-evento").addEventListener("click", async () => {
                if (!confirm("Deseja realmente excluir este evento?")) return;

                const respostaEx = await fetch(`/agenda/editar/${ev.id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ visivel: false })
                });

                if (respostaEx.ok) {
                    alert("Evento excluído!");
                    carregarEventosParaEdicao();
                } else {
                    alert("Erro ao excluir evento.");
                }
            });
        });

    } catch (erro) {
        console.error("Erro ao carregar eventos:", erro);
        listaEditar.innerHTML = "<p class='text-red-500'>Erro ao buscar eventos.</p>";
    }
}