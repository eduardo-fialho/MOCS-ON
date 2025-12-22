let consultasAtuais = [];
let consultaSelecionada = null;
let abaAtual = 'todas';

async function fetchConsultas() {
    try {
        const res = await fetch('/api/consultas');

        if (!res.ok) throw new Error('Erro ao buscar consultas');

        consultasAtuais = await res.json();
        const consultasAprovadas = consultasAtuais.filter(c => c.status === "APROVADA");
        renderListaConsultas(consultasAprovadas);

    } catch (e) {
        console.error(e);
    }
}

function renderListaConsultas(consultas) {
    const container = document.getElementById('lista-consultas');
    if (!container) return;

    if (!consultas.length) {
        container.innerHTML = `
            <p class="text-sm text-slate-400 italic">
                Nenhuma consulta disponível.
            </p>
        `;
        return;
    }

    container.innerHTML = consultas.map(c => 
        `
            <div class="border rounded-xl p-4 hover:bg-slate-50 cursor-pointer"
                onclick="carregarConsulta(${c.id})">

                <span class="text-xs px-2 py-1 rounded-full
                        ${c.status === 'APROVADA'
                            ? 'bg-green-100 text-green-700'
                            : c.status === 'PENDENTE'
                                ? 'bg-amber-100 text-amber-700'
                                : 'bg-slate-200 text-slate-600'}">
                        ${c.status}
                </span>
                <h3 class="font-semibold">${c.titulo}</h3>
                
                <p class="text-sm text-slate-600 mt-1">
                    ${c.pergunta}
                </p>
            </div>
    `).join('');
}

async function carregarConsulta(id) {
    try {
        const res = await fetch(`api/consultas/${id}`, {
            credentials: 'include'
        });

        if (!res.ok) throw new Error('Erro ao carregar consulta');

        const consulta = await res.json();
        consultaSelecionada = consulta;
        renderDetalheConsulta(consulta);

    } catch (e) {
        console.error(e);
    }
}

async function renderDetalheConsulta(c) {
    const container = document.getElementById('consulta-detalhe');
    if (!container) return;

    const usuario = await getUsuarioLogado();
    const podeAdministrar = usuario?.isSecretario && c.status.toLowerCase() === 'pendente';

    let favor = 0;
    let contra = 0;

    try {
        const res = await fetch(`/consultas/${c.id}/votos`, { credentials: 'include' });
        if (!res.ok) throw new Error('Erro ao buscar votos');

        const votos = await res.json();

        favor = votos.favor;
        contra = votos.contra;
    } catch (e) {
        console.error(e);
    }

    const total = favor + contra;
    const percFavor = total ? ((favor / total) * 100).toFixed(1) : 0;
    const percContra = total ? ((contra / total) * 100).toFixed(1) : 0;

    let vencedor = 'Empate';
    if (favor > contra) vencedor = 'A favor';
    else if (contra > favor) vencedor = 'Contra';

    container.innerHTML = `
        <h2 class="text-xl font-bold">${c.titulo}</h2>

        <p class="mt-4">${c.pergunta}</p>

        <div class="flex gap-4 mt-6">
            <span class="text-green-700 font-semibold">
                Sim: ${favor} (${percFavor}%)
            </span>
            <span class="text-red-700 font-semibold">
                Não: ${contra} (${percContra}%)
            </span>
        </div>

        <div class="relative w-full h-4 bg-gray-300 rounded mt-2 overflow-hidden">
            <div class="absolute top-0 left-0 h-full bg-green-500" style="width: ${percFavor}%;"></div>
            <div class="absolute top-0 right-0 h-full bg-red-500" style="width: ${percContra}%;"></div>
        </div>
        
        <p class="mt-2 font-semibold">
            Resultado parcial: ${vencedor}
        </p>
    `;
}

function aprovarConsulta(id) {
    fetch(`/consultas/${id}/aprovar`, {
        method: 'POST',
        credentials: 'include'
    }).then(() => location.reload());
}

function rejeitarConsulta(id) {
    fetch(`/consultas/${id}/rejeitar`, {
        method: 'POST',
        credentials: 'include'
    }).then(() => location.reload());
}

function arquivarConsulta(id) {
    fetch(`/consultas/${id}/arquivar`, {
        method: 'POST',
        credentials: 'include'
    }).then(() => location.reload());
}

document.addEventListener('DOMContentLoaded', fetchConsultas);
