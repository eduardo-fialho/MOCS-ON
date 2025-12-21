document.addEventListener("DOMContentLoaded", function () {

    const tipo = document.getElementById("tipo");
    const secretariadoSection = document.getElementById("secretariadoSection");
    const secretariadoFuncao = document.getElementById("secretariadoFuncao");
    const textoPadrao = document.getElementById("textoPadrao");
    const textoDocente = document.getElementById("textoDocente");
    const textoTecnico = document.getElementById("textoTecnico");

    function toggleSecretariado() {
        if (tipo.value === "SECRETARIADO") {
            secretariadoSection.classList.remove("hidden");
        } else {
            secretariadoSection.classList.add("hidden");
        }
    }

    function atualizarTextoFuncao() {
        textoPadrao.classList.add("hidden");
        textoDocente.classList.add("hidden");
        textoTecnico.classList.add("hidden");

        if (secretariadoFuncao.value === "DOCENTE") {
            textoDocente.classList.remove("hidden");
        } else if (secretariadoFuncao.value === "TECNICO_ADMINISTRATIVO") {
            textoTecnico.classList.remove("hidden");
        } else {
            textoPadrao.classList.remove("hidden");
        }
    }

    toggleSecretariado();
    atualizarTextoFuncao();

    tipo.addEventListener("change", toggleSecretariado);
    secretariadoFuncao.addEventListener("change", atualizarTextoFuncao);
});