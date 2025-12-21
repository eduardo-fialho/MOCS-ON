const APP_CONTEXT_PATH = (() => {
  const meta = document.querySelector('meta[name="app-context-path"]');
  const raw = meta ? meta.content.trim() : '';
  if (!raw) {
    return '';
  }
  return raw.endsWith('/') ? raw.slice(0, -1) : raw;
})();
const API_BASE_URL = `${window.location.origin}${APP_CONTEXT_PATH}`;
const API_ENDPOINT = `${API_BASE_URL}/aviso`;
const USER_ENDPOINT = `${API_BASE_URL}/user`;
const POST_API_BASE = `${API_BASE_URL}/post`;
const USER_AVATAR_ENDPOINT = `${API_BASE_URL}/user/avatar`;
const DEFAULT_AVATAR_URL = `${API_BASE_URL}/images/avatar-default.svg`;
const ANON_AVATAR_URL = `${API_BASE_URL}/images/avatar-default.svg`;

function readCsrf() {
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');
  const token = tokenMeta ? tokenMeta.content : null;
  const header = headerMeta ? headerMeta.content : 'X-CSRF-TOKEN';
  return { token, header };
}
