let nomeUsuario = "Usuário";

function carregarUsuario() {
    fetch("/user")
        .then(res => res.json())
        .then(user => {
            nomeUsuario = user.nome || "Usuário";
        })
        .catch(() => {
            nomeUsuario = "Usuário";
        });
}

function mostrarMensagem(texto, classe) {
    const msg = document.getElementById("mensagem");
    msg.textContent = texto;
    msg.className = `block ${classe}`;
}

function enviarRelato() {
    const assunto = document.getElementById("assunto").value.trim();
    const relato = document.getElementById("relato").value.trim();

    if (!assunto || !relato) {
        mostrarMensagem("Preencha todos os campos.", "text-red-600");
        return;
    }

    fetch("/ouvidoria", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
        body: new URLSearchParams({
            autor: nomeUsuario,
            assunto,
            relato
        })
    })
    .then(res => {
        if (!res.ok) throw new Error();
        return res.text();
    })
    .then(() => {
        mostrarMensagem("Relato enviado com sucesso!", "text-green-600");
        setTimeout(() => window.location.href = "/relatos_ouvidoria.html", 1500);
    })
    .catch(() => {
        mostrarMensagem("Erro ao enviar relato.", "text-red-600");
    });
}

document.addEventListener("DOMContentLoaded", () => {
    carregarUsuario();
    document.getElementById("btn-enviar").addEventListener("click", enviarRelato);
});
