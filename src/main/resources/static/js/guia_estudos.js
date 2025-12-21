document.addEventListener("DOMContentLoaded", () => {

    const guiaTitulo = document.getElementById("guia-titulo");
    const guiaComite = document.getElementById("guia-comite");
    const guiaRegras = document.getElementById("guia-regras");
    const guiaConteudo = document.getElementById("guia-conteudo");
    const guiaLinks = document.getElementById("guia-links");
    const pdfContainer = document.getElementById("pdf-container");
    const guiaPdfIframe = document.getElementById("guia-pdf");
    const downloadBtn = document.getElementById("download-btn");
    const guiaNaoOficial = document.getElementById("guia-nao-oficial");

    function getGuiaId() {
        const params = new URLSearchParams(window.location.search);
        return params.get("id");
    }

    async function carregarGuia() {
        const id = getGuiaId();
        if (!id) {
            alert("ID do guia não fornecido.");
            return;
        }

        try {
            const res = await fetch(`/guia-estudos/${id}`);
            if (!res.ok) throw new Error("Erro ao carregar guia");

            const guia = await res.json();
            if (!guia) {
                alert("Guia não encontrado.");
                return;
            }

            guiaTitulo.textContent = guia.titulo || "";
            guiaComite.textContent = guia.comiteNome || "";

            guiaRegras.innerHTML = guia.regras
                ? guia.regras.replace(/\n/g, '<br>')
                : "<p>Sem regras disponíveis</p>";

            guiaConteudo.innerHTML = guia.conteudo
                ? guia.conteudo.replace(/\n/g, '<br>')
                : "<p>Sem conteúdo disponível</p>";

            guiaLinks.innerHTML = "";
            if (guia.links && guia.links.length > 0) {
                guia.links.forEach(link => {
                    const li = document.createElement("li");
                    li.className = "break-words max-w-full";
                    const a = document.createElement("a");
                    a.href = link.link;
                    a.textContent = link.link;
                    a.target = "_blank";
                    a.className = "text-blue-600 hover:text-blue-800 hover:underline transition break-words";
                    li.appendChild(a);
                    guiaLinks.appendChild(li);
                });
            } else {
                guiaLinks.innerHTML = "<li>Nenhum link externo fornecido.</li>";
            }

            if (guia.arquivo) {
                pdfContainer.style.display = "block";
                guiaPdfIframe.src = `/guia-estudos/${id}/visualizar`;

                if (guia.oficial) {
                    downloadBtn.style.display = "flex";
                    downloadBtn.href = `/guia-estudos/${id}/arquivo`;
                    guiaNaoOficial.style.display = "none";
                } else {
                    downloadBtn.style.display = "none";
                    guiaNaoOficial.style.display = "block";
                }
            } else {
                pdfContainer.style.display = "none";
                guiaPdfIframe.src = "";
                downloadBtn.style.display = "none";
                guiaNaoOficial.style.display = "none";
            }

        } catch (err) {
            console.error(err);
            alert("Não foi possível carregar os dados do guia.");
        }
    }

    carregarGuia();
});