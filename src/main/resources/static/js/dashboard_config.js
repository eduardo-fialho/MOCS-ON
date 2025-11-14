const APP_CONTEXT_PATH = (() => {
  const path = window.location.pathname || "";
  const base = path.replace(/\/[^/]*$/, "");
  if (!base || base === "/" || base === path) return "";
  return base;
})();

const API_BASE_URL = `${window.location.origin}${APP_CONTEXT_PATH}`;
const API_ENDPOINT = `${API_BASE_URL}/aviso`;
const USER_ENDPOINT = `${API_BASE_URL}/user`;
const POST_API_BASE = `${API_BASE_URL}/post`;
