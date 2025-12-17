document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-materia');
    const messageArea = document.getElementById('message-area');

    form.addEventListener('submit', async (event) => {
        event.preventDefault(); 

        messageArea.classList.add('hidden'); 
        messageArea.textContent = '';
        messageArea.classList.remove('bg-green-100', 'bg-red-100', 'text-green-800', 'text-red-800');


        const formData = new FormData(form);
        console.log(formData);

        try {
            const response = await fetch('/materias', {
                method: 'POST',
                body: formData,
                credentials: 'include' 
            });

            if (!response.ok) {
                let errorMsg = `Erro HTTP ${response.status}`;
                try {
                    const data = await response.json();
                    if (data?.message) errorMsg = data.message;
                } catch (_) {}
                throw new Error(errorMsg);
            }


            messageArea.textContent = 'Matéria enviada com sucesso!';
            messageArea.classList.remove('hidden');
            messageArea.classList.add('bg-green-100', 'text-green-800');

            form.reset();
        } catch (err) {
            console.error('Erro ao enviar matéria:', err);
            messageArea.textContent = `Erro ao enviar matéria: ${err.message}`;
            messageArea.classList.remove('hidden');
            messageArea.classList.add('bg-red-100', 'text-red-800');
        }
    });
});
