const APP_CONTEXT_PATH = (() => {
    const path = window.location.pathname || '';
    const base = path.replace(/\/[^/]*$/, '');
    if (!base || base === '/' || base === path) {
        return '';
    }
    return base;
})();

const API_BASE_URL = `${window.location.origin}${APP_CONTEXT_PATH}`;
const PRESENCA_BASE = `${API_BASE_URL}/presencas`;

const STATUS_OPTIONS = [
    { value: 'PRESENTE', label: 'Presente' },
    { value: 'AUSENTE', label: 'Ausente' },
    { value: 'JUSTIFICADO', label: 'Justificado' },
    { value: 'ATRASO', label: 'Atraso' }
];

const STATUS_STYLES = {
    PRESENTE: 'bg-emerald-50 border-emerald-200 text-emerald-700',
    AUSENTE: 'bg-rose-50 border-rose-200 text-rose-700',
    JUSTIFICADO: 'bg-amber-50 border-amber-200 text-amber-700',
    ATRASO: 'bg-sky-50 border-sky-200 text-sky-700'
};

const els = {
    form: document.getElementById('form-presenca'),
    titulo: document.getElementById('presenca-titulo'),
    comite: document.getElementById('presenca-comite'),
    data: document.getElementById('presenca-data'),
    horaInicio: document.getElementById('presenca-hora-inicio'),
    horaFim: document.getElementById('presenca-hora-fim'),
    observacao: document.getElementById('presenca-observacao'),
    listas: document.getElementById('lista-presencas'),
    reloadListas: document.getElementById('recarregar-listas'),
    detalheTitulo: document.getElementById('presenca-detalhe-titulo'),
    detalheInfo: document.getElementById('presenca-detalhe-info'),
    resumo: document.getElementById('presenca-resumo'),
    salvar: document.getElementById('salvar-presenca'),
    registros: document.getElementById('presenca-registros'),
    busca: document.getElementById('presenca-busca'),
    filtroStatus: document.getElementById('presenca-filtro-status'),
    alert: document.getElementById('alert-presenca')
};

let listas = [];
let listaAtual = null;
let registros = [];
let registrosByUserId = new Map();

document.addEventListener('DOMContentLoaded', () => {
    if (els.form) {
        els.form.addEventListener('submit', handleCriarLista);
    }
    if (els.reloadListas) {
        els.reloadListas.addEventListener('click', () => carregarListas(true));
    }
    if (els.salvar) {
        els.salvar.addEventListener('click', salvarRegistros);
    }
    if (els.busca) {
        els.busca.addEventListener('input', renderRegistros);
    }
    if (els.filtroStatus) {
        els.filtroStatus.addEventListener('change', renderRegistros);
    }

    carregarComites();
    carregarListas();
});

async function carregarComites() {
    if (!els.comite) {
        return;
    }
    try {
        const response = await fetch(`${PRESENCA_BASE}/comites`);
        if (!response.ok) {
            return;
        }
        const comites = await response.json();
        els.comite.innerHTML = '<option value="">Sem comite</option>';
        comites.forEach(comite => {
            const option = document.createElement('option');
            option.value = String(comite.id);
            option.textContent = formatComiteLabel(comite);
            els.comite.appendChild(option);
        });
    } catch (err) {
        console.warn('Falha ao carregar comites', err);
    }
}

async function carregarListas(force) {
    try {
        const response = await fetch(`${PRESENCA_BASE}/listas`);
        if (!response.ok) {
            throw new Error('Falha ao buscar listas');
        }
        listas = await response.json();
        renderListas();
        if (listaAtual && !force) {
            return;
        }
        if (listas.length > 0) {
            selecionarLista(listas[0].id);
        } else {
            limparDetalhe();
        }
    } catch (err) {
        console.warn(err);
        if (els.listas) {
            els.listas.textContent = 'Nao foi possivel carregar as listas.';
        }
    }
}

function renderListas() {
    if (!els.listas) {
        return;
    }
    els.listas.innerHTML = '';
    if (!listas.length) {
        els.listas.textContent = 'Nenhuma lista cadastrada.';
        return;
    }
    listas.forEach(lista => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `w-full text-left border rounded-xl p-3 transition ${
            listaAtual && listaAtual.id === lista.id ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-blue-300'
        }`;
        button.innerHTML = `
            <div class="font-semibold text-slate-900">${escapeHtml(lista.titulo || 'Lista')}</div>
            <div class="text-xs text-slate-500 mt-1">${escapeHtml(formatListaInfo(lista))}</div>
        `;
        button.addEventListener('click', () => selecionarLista(lista.id));
        els.listas.appendChild(button);
    });
}

async function selecionarLista(id) {
    if (!id) {
        return;
    }
    try {
        const response = await fetch(`${PRESENCA_BASE}/listas/${id}`);
        if (!response.ok) {
            throw new Error('Lista nao encontrada');
        }
        const data = await response.json();
        listaAtual = data.lista;
        registros = Array.isArray(data.registros) ? data.registros : [];
        registrosByUserId = new Map();
        registros.forEach(registro => {
            if (registro.usuarioId != null) {
                registrosByUserId.set(String(registro.usuarioId), registro);
            }
        });
        renderDetalhe();
        renderListas();
        renderRegistros();
    } catch (err) {
        console.warn(err);
        showAlert('Nao foi possivel carregar a lista selecionada.', 'error');
    }
}

function renderDetalhe() {
    if (!listaAtual) {
        limparDetalhe();
        return;
    }
    if (els.detalheTitulo) {
        els.detalheTitulo.textContent = listaAtual.titulo || 'Lista de presenca';
    }
    if (els.detalheInfo) {
        els.detalheInfo.textContent = formatListaInfo(listaAtual);
    }
    if (els.salvar) {
        els.salvar.disabled = false;
    }
    atualizarResumo();
}

function limparDetalhe() {
    listaAtual = null;
    registros = [];
    registrosByUserId = new Map();
    if (els.detalheTitulo) {
        els.detalheTitulo.textContent = 'Selecione uma lista';
    }
    if (els.detalheInfo) {
        els.detalheInfo.textContent = 'Escolha uma lista para registrar presenca.';
    }
    if (els.resumo) {
        els.resumo.innerHTML = '';
    }
    if (els.salvar) {
        els.salvar.disabled = true;
    }
    if (els.registros) {
        els.registros.textContent = 'Nenhuma lista selecionada.';
    }
}

function atualizarResumo() {
    if (!els.resumo) {
        return;
    }
    const contagem = contarStatus(registros);
    const itens = [
        { label: 'Total', value: contagem.total, className: 'bg-slate-100 text-slate-700' },
        { label: 'Presentes', value: contagem.PRESENTE, className: 'bg-emerald-100 text-emerald-700' },
        { label: 'Ausentes', value: contagem.AUSENTE, className: 'bg-rose-100 text-rose-700' },
        { label: 'Justificados', value: contagem.JUSTIFICADO, className: 'bg-amber-100 text-amber-700' },
        { label: 'Atraso', value: contagem.ATRASO, className: 'bg-sky-100 text-sky-700' }
    ];
    els.resumo.innerHTML = itens.map(item => `
        <span class="px-3 py-1 rounded-full text-xs font-semibold ${item.className}">
            ${item.label}: ${item.value}
        </span>
    `).join('');
}

function renderRegistros() {
    if (!els.registros) {
        return;
    }
    if (!listaAtual) {
        els.registros.textContent = 'Nenhuma lista selecionada.';
        return;
    }
    const filtroTexto = (els.busca?.value || '').trim().toLowerCase();
    const filtroStatus = (els.filtroStatus?.value || '').trim().toUpperCase();
    const filtrados = registros.filter(registro => {
        const nome = (registro.usuarioNome || '').toLowerCase();
        const email = (registro.usuarioEmail || '').toLowerCase();
        const matchTexto = !filtroTexto || nome.includes(filtroTexto) || email.includes(filtroTexto);
        const status = normalizeStatus(registro.status);
        const matchStatus = !filtroStatus || status === filtroStatus;
        return matchTexto && matchStatus;
    });

    if (!filtrados.length) {
        els.registros.textContent = 'Nenhum participante encontrado para os filtros selecionados.';
        return;
    }

    const rows = filtrados.map(registro => {
        const status = normalizeStatus(registro.status);
        const statusClass = STATUS_STYLES[status] || STATUS_STYLES.AUSENTE;
        const options = STATUS_OPTIONS.map(opt => `
            <option value="${opt.value}" ${opt.value === status ? 'selected' : ''}>${opt.label}</option>
        `).join('');
        return `
            <tr data-user-id="${registro.usuarioId}">
                <td class="py-3 pr-3">
                    <div class="font-semibold text-slate-900">${escapeHtml(registro.usuarioNome || 'Sem nome')}</div>
                    <div class="text-xs text-slate-500">${escapeHtml(registro.usuarioEmail || '')}</div>
                </td>
                <td class="py-3 pr-3">
                    <select data-status class="w-full px-3 py-2 border rounded-xl text-sm font-semibold ${statusClass}">
                        ${options}
                    </select>
                </td>
                <td class="py-3">
                    <input data-observacao type="text" class="w-full px-3 py-2 border border-slate-200 rounded-xl" value="${escapeHtml(registro.observacao || '')}" placeholder="Observacao">
                </td>
            </tr>
        `;
    }).join('');

    els.registros.innerHTML = `
        <div class="overflow-auto">
            <table class="min-w-full text-sm">
                <thead>
                    <tr class="text-left text-slate-500 border-b">
                        <th class="pb-2">Participante</th>
                        <th class="pb-2">Status</th>
                        <th class="pb-2">Observacao</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    ${rows}
                </tbody>
            </table>
        </div>
    `;

    els.registros.querySelectorAll('select[data-status]').forEach(select => {
        select.addEventListener('change', handleStatusChange);
    });
    els.registros.querySelectorAll('input[data-observacao]').forEach(input => {
        input.addEventListener('input', handleObservacaoChange);
    });
}

function handleStatusChange(event) {
    const select = event.target;
    const row = select.closest('tr');
    if (!row) {
        return;
    }
    const userId = row.dataset.userId;
    const registro = registrosByUserId.get(String(userId));
    if (!registro) {
        return;
    }
    registro.status = select.value;
    atualizarResumo();
    atualizarClasseStatus(select);
}

function handleObservacaoChange(event) {
    const input = event.target;
    const row = input.closest('tr');
    if (!row) {
        return;
    }
    const userId = row.dataset.userId;
    const registro = registrosByUserId.get(String(userId));
    if (!registro) {
        return;
    }
    registro.observacao = input.value;
}

function atualizarClasseStatus(select) {
    const status = normalizeStatus(select.value);
    select.className = `w-full px-3 py-2 border rounded-xl text-sm font-semibold ${STATUS_STYLES[status] || STATUS_STYLES.AUSENTE}`;
}

async function salvarRegistros() {
    if (!listaAtual || !listaAtual.id) {
        return;
    }
    if (els.salvar) {
        els.salvar.disabled = true;
        els.salvar.textContent = 'Salvando...';
    }
    try {
        const payload = {
            registros: registros.map(registro => ({
                usuarioId: registro.usuarioId,
                usuarioNome: registro.usuarioNome,
                usuarioEmail: registro.usuarioEmail,
                status: normalizeStatus(registro.status),
                observacao: registro.observacao || null
            }))
        };
        const response = await fetch(`${PRESENCA_BASE}/listas/${listaAtual.id}/registros`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            throw new Error('Erro ao salvar');
        }
        showAlert('Lista salva com sucesso.', 'success');
    } catch (err) {
        console.warn(err);
        showAlert('Nao foi possivel salvar a lista.', 'error');
    } finally {
        if (els.salvar) {
            els.salvar.disabled = false;
            els.salvar.textContent = 'Salvar lista';
        }
    }
}

async function handleCriarLista(event) {
    event.preventDefault();
    const titulo = (els.titulo?.value || '').trim();
    const dataSessao = (els.data?.value || '').trim();
    if (!titulo || !dataSessao) {
        showAlert('Informe titulo e data da sessao.', 'error');
        return;
    }
    const payload = {
        titulo,
        dataSessao,
        horaInicio: (els.horaInicio?.value || '').trim() || null,
        horaFim: (els.horaFim?.value || '').trim() || null,
        observacao: (els.observacao?.value || '').trim() || null,
        comiteId: els.comite?.value ? Number(els.comite.value) : null
    };
    try {
        const response = await fetch(`${PRESENCA_BASE}/listas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Erro ao criar');
        }
        const nova = await response.json();
        showAlert('Lista criada com sucesso.', 'success');
        limparFormulario();
        await carregarListas(true);
        if (nova && nova.id) {
            selecionarLista(nova.id);
        }
    } catch (err) {
        console.warn(err);
        showAlert('Nao foi possivel criar a lista.', 'error');
    }
}

function limparFormulario() {
    if (els.form) {
        els.form.reset();
    }
}

function contarStatus(lista) {
    const contagem = {
        total: lista.length,
        PRESENTE: 0,
        AUSENTE: 0,
        JUSTIFICADO: 0,
        ATRASO: 0
    };
    lista.forEach(registro => {
        const status = normalizeStatus(registro.status);
        if (contagem[status] != null) {
            contagem[status] += 1;
        }
    });
    return contagem;
}

function normalizeStatus(status) {
    if (!status) {
        return 'AUSENTE';
    }
    return String(status).trim().toUpperCase();
}

function formatListaInfo(lista) {
    const data = lista.dataSessao ? lista.dataSessao : '';
    const hora = lista.horaInicio ? ` ${lista.horaInicio}` : '';
    const comite = formatListaComite(lista);
    const partes = [];
    if (data) {
        partes.push(data + hora);
    }
    if (comite) {
        partes.push(comite);
    }
    return partes.join(' • ') || 'Sem dados';
}

function formatListaComite(lista) {
    const sigla = lista.comiteSigla ? String(lista.comiteSigla).trim() : '';
    const nome = lista.comiteNome ? String(lista.comiteNome).trim() : '';
    if (sigla && nome) {
        return `${sigla} - ${nome}`;
    }
    if (nome) {
        return nome;
    }
    if (sigla) {
        return sigla;
    }
    return 'Sem comite';
}

function formatComiteLabel(comite) {
    const sigla = comite.sigla ? String(comite.sigla).trim() : '';
    const nome = comite.nome ? String(comite.nome).trim() : '';
    if (sigla && nome) {
        return `${sigla} - ${nome}`;
    }
    return nome || sigla || 'Comite';
}

function showAlert(message, type) {
    if (!els.alert) {
        return;
    }
    const base = 'rounded-xl px-4 py-3 text-sm font-semibold';
    const style = type === 'error'
        ? 'bg-rose-100 text-rose-700'
        : 'bg-emerald-100 text-emerald-700';
    els.alert.className = `${base} ${style}`;
    els.alert.textContent = message;
    els.alert.classList.remove('hidden');
    clearTimeout(showAlert.timer);
    showAlert.timer = setTimeout(() => {
        els.alert.classList.add('hidden');
    }, 3500);
}

function escapeHtml(value) {
    return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
