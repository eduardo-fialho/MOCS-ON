let usuarioCache = null;

async function getUsuarioLogado() {
    if (usuarioCache) return usuarioCache;

    const response = await fetch(USER_ENDPOINT, {
        credentials: 'include'
    });

    if (!response.ok) return null;

    usuarioCache = await response.json();
    return usuarioCache;
}
