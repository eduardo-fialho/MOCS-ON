document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-consulta');
    const messageArea = document.getElementById('message-area');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        messageArea.classList.add('hidden');
        messageArea.textContent = '';

        const formData = new FormData(form);

        try {
            const res = await fetch('/consultas', {
                method: 'POST',
                body: new URLSearchParams(formData),
                credentials: 'include'
            });

            if (!res.ok) throw new Error('Erro ao criar consulta');

            messageArea.textContent = 'Consulta criada com sucesso!';
            messageArea.classList.remove('hidden');
            messageArea.classList.add('bg-green-100', 'text-green-800');

            form.reset();

        } catch (e) {
            messageArea.textContent = 'Erro ao criar consulta';
            messageArea.classList.remove('hidden');
            messageArea.classList.add('bg-red-100', 'text-red-800');
        }
    });
});
