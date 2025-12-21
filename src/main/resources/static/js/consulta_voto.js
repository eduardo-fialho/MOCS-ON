async function votarConsulta(consultaId, voto) {
    try {
        const res = await fetch(`/consultas/${consultaId}/votar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `voto=${voto}`,
            credentials: 'include'
        });

        if (!res.ok) throw new Error('Erro ao votar');

        location.reload();

    } catch (e) {
        console.error(e);
        alert('Não foi possível registrar o voto');
    }
}
