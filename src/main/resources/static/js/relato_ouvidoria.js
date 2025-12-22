// Função para verificar se o usuário é secretário
function verificarSeSecretario() {
    // Faz uma requisição para o endpoint '/user'
    fetch('/user')
        .then(response => response.json()) // Espera pela resposta JSON
        .then(data => {
            // Se o usuário for secretário, mostra o painel de secretário
            if (data.isSecretario) {
                document.getElementById('secretarioPanel').classList.remove('hidden');
            } else {
                document.getElementById('secretarioPanel').classList.add('hidden');
            }
        })
        .catch(error => {
            console.error('Erro ao verificar o usuário:', error);
        });
}

// Função para marcar o relato como resolvido
function marcarResolvido(id) {
    const autor = "SECRETARIO"; // O nome do cargo que está fazendo a ação

    // Faz a requisição para marcar o relato como resolvido
    fetch(`/ouvidoria/${id}/responder?ouvidor=${autor}&resposta=Resolvido&status=RESOLVIDO`, {
        method: 'POST'
    })
    .then(res => {
        if (res.ok) {
            alert("Relato marcado como resolvido!");
            location.reload(); // Recarrega a página após sucesso
        } else {
            alert('Erro ao marcar como resolvido.');
        }
    });
}

// Função para arquivar o relato
function arquivarRelato(id) {
    // Confirmação de arquivamento
    if (!confirm('Tem certeza que deseja arquivar este relato?')) return;

    // Requisição para arquivar o relato
    fetch(`/ouvidoria/${id}/deletar`, { method: 'GET' })
        .then(res => {
            if (res.ok) {
                alert('Relato arquivado com sucesso!');
                location.href = '/relatos_ouvidoria.html'; // Redireciona para a página de relatos
            } else {
                alert('Erro ao arquivar relato.');
            }
        });
}

// Função para enviar comentário do secretário
function enviarComentario(id) {
    const comentario = document.getElementById('comentarioText').value.trim();

    // Verifica se o campo de comentário está vazio
    if (!comentario) {
        alert('Digite um comentário antes de enviar.');
        return;
    }

    const autor = "SECRETARIO"; // O nome do cargo que está fazendo a ação

    // Envia o comentário do secretário para o backend
    fetch(`/ouvidoria/${id}/responder?ouvidor=${autor}&resposta=${encodeURIComponent(comentario)}&status=RESOLVIDO`, {
        method: 'POST'
    })
    .then(res => {
        if (res.ok) {
            alert("Comentário enviado com sucesso!");
            location.reload(); // Recarrega a página para exibir o comentário
        } else {
            alert('Erro ao enviar comentário.');
        }
    });
}

// Chama a função assim que a página carrega para verificar o tipo de usuário
document.addEventListener('DOMContentLoaded', function() {
    verificarSeSecretario(); // Verifica se é secretário ou não
});
