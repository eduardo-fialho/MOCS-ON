function guiaData() {
    return {
        guiaId: null,
        guiaTitle: 'Guia de Estudo Padrão - 2025',
        guiaComite: 'CSNU - Conselho de Segurança das Nações Unidas',
        guiaStatus: 'APROVADO',
        guiaRegras: 'As regras de submissão e debate seguem o ROP 5.2. O documento de posição deve ter no máximo 1500 palavras, formatado em Times New Roman, tamanho 12.',
        guiaConteudo: 'O tema principal é a "Reforma do Conselho de Segurança". Os tópicos a serem abordados incluem: composição, poder de veto e mecanismos de resposta rápida a crises.',
        guiaPdfUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', // Link de exemplo
        guiaLinks: [
            { nome: 'Site Oficial da ONU', url: 'https://www.un.org/' },
            { nome: 'Estatuto de Roma', url: 'https://www.icc-cpi.int/rome-statute' },
        ],
        loadGuiaDetails() {
            // Simulação: buscar ID da URL e carregar dados via fetch (como em documentos.js)
            const urlParams = new URLSearchParams(window.location.search);
            this.guiaId = urlParams.get('guiaId') || 1;

            // Em uma aplicação real, você faria um FETCH aqui:
            // fetch(`/api/guias/${this.guiaId}`)
            // .then(res => res.json())
            // .then(data => { /* atribuir data às propriedades */ });
        }
    }
}