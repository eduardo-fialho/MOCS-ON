let consultasAtuais = [];
let consultaSelecionada = null;
let abaAtual = 'todas';

function mostrarAba(aba) {
    abaAtual = aba;

    
    document.getElementById('btn-todas').className = aba==='todas' 
        ? 'px-4 py-2 font-semibold border-b-2 text-blue-600 border-blue-600'
        : 'px-4 py-2 font-semibold border-b-2 text-slate-500';

    document.getElementById('btn-pendentes').className = aba==='pendente'
        ? 'px-4 py-2 font-semibold border-b-2 text-amber-600 border-amber-500'
        : 'px-4 py-2 font-semibold border-b-2 text-slate-500';

    document.getElementById('btn-aprovadas').className = aba==='aprovada'
        ? 'px-4 py-2 font-semibold border-b-2 text-green-600 border-green-600'
        : 'px-4 py-2 font-semibold border-b-2 text-slate-500';

    if (aba === 'todas') {
        renderListaConsultas(consultasAtuais);
    } else {
        renderListaConsultas(consultasAtuais.filter(c => c.status === aba.toUpperCase()));
    }
}


async function fetchConsultas() {
    try {
        const res = await fetch('/api/consultas', {
            credentials: 'include'
        });

        if (!res.ok) throw new Error('Erro ao buscar consultas');

        consultasAtuais = await res.json();
        renderListaConsultas(consultasAtuais);

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

        <p class="mt-2 font-semibold">
            Resultado parcial: ${vencedor}
        </p>

        <div class="mt-6 flex gap-3">
            ${podeAdministrar ? `
                <button onclick="aprovarConsulta(${c.id})"
                        class="bg-green-600 text-white px-4 py-2 rounded-lg">
                    Aprovar
                </button>

                <button onclick="rejeitarConsulta(${c.id})"
                        class="bg-red-600 text-white px-4 py-2 rounded-lg">
                    Rejeitar
                </button>
            ` : ''}

            ${c.status.toLowerCase() === 'aprovada' ? `
                <button onclick="arquivarConsulta(${c.id})"
                        class="bg-orange-600 text-white px-4 py-2 rounded-lg">
                    Arquivar
                </button>
            ` : ''}
        </div>
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
