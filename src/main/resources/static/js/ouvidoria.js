document.addEventListener("DOMContentLoaded", function () {
    const identificacaoSelect = document.getElementById("identificacao");
    const dadosIdentificacao = document.getElementById("dados-identificacao");
    const campoNome = document.getElementById("campo-nome");
    const categoriaSelect = document.getElementById("categoria_relato");
    const secaoComite = document.getElementById("secao-comite");
    const secaoSecretariado = document.getElementById("secao-secretariado");
    const secaoOutros = document.getElementById("secao-outros");
    const btnEnviar = document.getElementById("btn-enviar");
    const msgSucesso = document.getElementById("msg-sucesso");
    const msgErro = document.getElementById("msg-erro");
    identificacaoSelect.addEventListener("change", function () {
        if (this.value === "anonimo") {
            dadosIdentificacao.classList.add("hidden");
            campoNome.classList.add("hidden");
        } else {
            dadosIdentificacao.classList.remove("hidden");
            campoNome.classList.toggle("hidden", this.value !== "comite_e_nome");
        }
    });
    categoriaSelect.addEventListener("change", function () {
        secaoComite.classList.add("hidden");
        secaoSecretariado.classList.add("hidden");
        secaoOutros.classList.add("hidden");
        btnEnviar.classList.add("hidden");
        if (this.value === "comite") secaoComite.classList.remove("hidden");
        if (this.value === "secretariado") secaoSecretariado.classList.remove("hidden");
        if (this.value === "outros") secaoOutros.classList.remove("hidden");
        if (this.value !== "") btnEnviar.classList.remove("hidden");
    });
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("success")) msgSucesso.classList.remove("hidden");
    else if (urlParams.has("error")) msgErro.classList.remove("hidden");
});
