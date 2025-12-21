document.addEventListener("DOMContentLoaded", init);

let usuario = null;

function init() {
    carregarUsuario();
}

function carregarUsuario() {
    fetch("/user")
        .then(res => res.json())
        .then(data => {
            usuario = data;
            carregarRelatos();
        })
        .catch(err => {
            console.error("Erro ao carregar informações do usuário:", err);
            document.getElementById("lista-relatos").innerHTML =
                "<p class='text-red-500'>Erro ao carregar informações do usuário.</p>";
        });
}

function carregarRelatos() {
    fetch("/ouvidoria") // Endpoint que retorna todos os relatos
        .then(res => res.json())
        .then(relatos => {
            const lista = document.getElementById("lista-relatos");
            lista.innerHTML = "";

            if (!relatos || relatos.length === 0) {
                lista.innerHTML = "<p class='text-slate-400'>Nenhum relato encontrado.</p>";
                return;
            }

            relatos.forEach(r => {
                // Se o usuário não for secretário, só mostra seus próprios relatos
                if (!usuario.isSecretario && r.autor !== usuario.nome) {
                    return;
                }

                const card = document.createElement("a");
                card.href = `/relato_ouvidoria.html?id=${r.id}`;
                card.className =
                    "block bg-white border border-slate-200 rounded-xl p-4 " +
                    "hover:bg-slate-50 hover:border-blue-400 transition shadow-sm";

                card.innerHTML = `
                    <div class="flex justify-between items-start gap-3">
                        <div>
                            <h3 class="font-bold text-slate-800">${r.assunto}</h3>
                            <p class="text-sm text-slate-500 mt-1">
                                Autor: ${r.autor || 'Anônimo'}
                            </p>
                        </div>
                        <span class="text-xs font-semibold px-3 py-1 rounded-full
                            ${r.status === 'ABERTO' ? 'bg-amber-100 text-amber-700' :
                              r.status === 'RESPONDIDO' ? 'bg-green-100 text-green-700' :
                              'bg-slate-200 text-slate-700'}">
                            ${r.status}
                        </span>
                    </div>
                    <p class="mt-2 text-sm text-slate-600 line-clamp-2">
                        ${r.relato || ''}
                    </p>
                `;

                lista.appendChild(card);
            });
        })
        .catch(err => {
            console.error("Erro ao carregar relatos:", err);
            document.getElementById("lista-relatos").innerHTML =
                "<p class='text-red-500'>Erro ao carregar relatos.</p>";
        });
}
