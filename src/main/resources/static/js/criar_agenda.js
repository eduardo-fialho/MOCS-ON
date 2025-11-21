document.getElementById("form-criar-evento").addEventListener("submit", async function (e) {
    e.preventDefault();

    const dados = {
        titulo: document.getElementById("titulo").value,
        descricao: document.getElementById("descricao").value,
        data_evento: document.getElementById("data_evento").value,
        hora_evento: document.getElementById("hora_evento").value
    };
    console.log(dados);
    console.log(JSON.stringify(dados));
    const resposta = await fetch("/agenda/criar", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(dados)
    });

    if (resposta.ok) {
        alert("Evento criado com sucesso!");
        document.getElementById("form-criar-evento").reset();
    } else {
        alert("Erro ao criar evento.");
    }
});

document.getElementById("botao-abrir-form")?.addEventListener("click", () => {
    document.getElementById("form-criar-evento").classList.remove("hidden");
    document.getElementById("botao-abrir-form").classList.add("hidden");
});

document.getElementById("botao-cancelar-form")?.addEventListener("click", () => {
    document.getElementById("form-criar-evento").classList.add("hidden");
    document.getElementById("botao-abrir-form").classList.remove("hidden");
});


document.addEventListener("DOMContentLoaded", () => {
    const abrirFormEventoBtn = document.getElementById("abrir-form-evento");
    const formEventoContainer = document.getElementById("form-evento-container");

    abrirFormEventoBtn.addEventListener("click", () => {
        formEventoContainer.classList.toggle("hidden");
    });
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