function cadastroGuia() {
    return {
        isSubmitting: false,
        form: {
            titulo: '',
            comite: '',
            regras: '',
            conteudo: '',
            pdfFile: null,
            links: [],
        },
        addLink() {
            this.form.links.push({ nome: '', url: '' });
        },
        removeLink(index) {
            this.form.links.splice(index, 1);
        },
        handleFileUpload(event) {
            this.form.pdfFile = event.target.files[0];
        },
        async submitGuia() {
            this.isSubmitting = true;
            // Lógica de envio:

            // 1. Criar FormData para enviar dados e o arquivo
            const formData = new FormData();
            formData.append('titulo', this.form.titulo);
            formData.append('comite', this.form.comite);
            formData.append('regras', this.form.regras);
            formData.append('conteudo', this.form.conteudo);
            if (this.form.pdfFile) {
                formData.append('pdfFile', this.form.pdfFile);
            }
            formData.append('links', JSON.stringify(this.form.links)); // Links como JSON string

            try {
                // Substituir pelo seu endpoint de API real
                const response = await fetch('http://localhost:8082/guias_estudo/submeter', {
                    method: 'POST',
                    body: formData, // Envio via FormData para incluir o arquivo
                });

                if (!response.ok) {
                    throw new Error('Erro ao submeter o guia. Verifique os dados.');
                }

                alert('Guia de Estudo submetido com sucesso! Será encaminhado para avaliação.');
                window.location.href = '/guias_estudo.html'; // Redireciona para a lista

            } catch (error) {
                console.error('Erro de submissão:', error);
                alert(`Falha na submissão: ${error.message}`);
            } finally {
                this.isSubmitting = false;
            }
        }
    }
}