document.addEventListener("DOMContentLoaded", function () {
    const tipo = document.getElementById("tipo");
    const secretariadoSection = document.getElementById("secretariadoSection");
    const secretariadoFuncao = document.getElementById("secretariadoFuncao");
    const comiteSection = document.getElementById("comiteSection");
    const comiteInputs = comiteSection
        ? comiteSection.querySelectorAll('input[type="checkbox"]')
        : [];

    const textoPadrao = document.getElementById("textoPadrao");
    const textoDocente = document.getElementById("textoDocente");
    const textoTecnico = document.getElementById("textoTecnico");

    function atualizarSecao() {
        const isSecretariado = tipo.value === "SECRETARIADO";
        if (isSecretariado) {
            secretariadoSection.classList.remove("hidden");
        } else {
            secretariadoSection.classList.add("hidden");
        }
        if (comiteSection) {
            comiteSection.classList.toggle("hidden", isSecretariado);
        }
        if (comiteInputs && comiteInputs.length) {
            comiteInputs.forEach((input) => {
                input.disabled = isSecretariado;
            });
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

    atualizarSecao();
    atualizarTextoFuncao();

    tipo.addEventListener("change", atualizarSecao);
    secretariadoFuncao.addEventListener("change", atualizarTextoFuncao);
});
