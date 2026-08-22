
const API_BASE_URL = window.APP_CONFIG?.API_BASE_URL || '/api';

const Storage = {
  getToken: () => localStorage.getItem('mt_token'),
  setToken: (token) => localStorage.setItem('mt_token', token),
  getUser: () => JSON.parse(localStorage.getItem('mt_user') || 'null'),
  setUser: (user) => localStorage.setItem('mt_user', JSON.stringify(user)),
  clear: () => {
    localStorage.removeItem('mt_token');
    localStorage.removeItem('mt_user');
  }
};

async function apiRequest(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' };

  if (auth) {
    const token = Storage.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const user = Storage.getUser();
    if (user?.tenantId) headers['X-Tenant-Id'] = user.tenantId;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  const payload = await response.json().catch(() => ({}));

  if (!response.ok || payload.success === false) {
    const message = payload.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return payload.data;
}

const Api = {
  register: (data) => apiRequest('/auth/register', { method: 'POST', body: data, auth: false }),
  login: (data) => apiRequest('/auth/login', { method: 'POST', body: data, auth: false }),

  createTenant: (data) => apiRequest('/tenants', { method: 'POST', body: data, auth: false }),

  getProjects: () => apiRequest('/projects'),
  getProject: (id) => apiRequest(`/projects/${id}`),
  createProject: (data) => apiRequest('/projects', { method: 'POST', body: data }),
  updateProject: (id, data) => apiRequest(`/projects/${id}`, { method: 'PUT', body: data }),
  deleteProject: (id) => apiRequest(`/projects/${id}`, { method: 'DELETE' })
};

function requireAuth() {
  if (!Storage.getToken()) {
    window.location.href = 'index.html';
  }
}

function logout() {
  Storage.clear();
  window.location.href = 'index.html';
}
