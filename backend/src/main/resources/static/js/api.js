const API_BASE = '';

function getToken() {
    return localStorage.getItem('ferwafa_token');
}

function setToken(token) {
    if (token) {
        localStorage.setItem('ferwafa_token', token);
    } else {
        localStorage.removeItem('ferwafa_token');
    }
}

function clearAuthStorage() {
    localStorage.removeItem('ferwafa_token');
    localStorage.removeItem('ferwafa_role');
    localStorage.removeItem('ferwafa_entity_id');
}

async function logout() {
    try {
        await fetch(API_BASE + '/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch (_) { /* ignore */ }
    clearAuthStorage();
    window.location.href = '/login';
}

async function apiCall(method, url, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const opts = { method, headers, credentials: 'include' };
    if (body !== undefined && body !== null) opts.body = JSON.stringify(body);

    const res = await fetch(API_BASE + url, opts);
    const contentType = res.headers.get('content-type') || '';
    const isAuthLogin = url.includes('/auth/login') || url.includes('/auth/referee-login');

    if (contentType.includes('application/pdf') || contentType.includes('octet-stream')) {
        if (!res.ok) throw new Error('Request failed');
        return res.blob();
    }

    const data = await res.json().catch(() => null);

    if (!res.ok) {
        const message = (data && (data.message || data.error)) || 'Request failed';
        // Never auto-logout on the login request itself
        if (!isAuthLogin && res.status === 401) {
            clearAuthStorage();
            window.location.href = '/login?error=login_required';
            throw new Error(message);
        }
        throw new Error(message);
    }
    return data;
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = 'alert alert-' + type + ' position-fixed bottom-0 end-0 m-3';
    toast.style.zIndex = 9999;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('error') === 'access_denied' || params.get('error') === 'login_required') {
        const box = document.getElementById('adminError') || document.getElementById('teamError');
        if (box) {
            box.textContent = params.get('error') === 'access_denied'
                ? 'Access denied. Log in with the correct account (Admin vs Team).'
                : 'Please log in to continue.';
            box.classList.remove('d-none');
        }
    }

    const path = window.location.pathname;
    if (path === '/login' || path === '/') {
        const token = getToken();
        if (!token) return;
        try {
            const me = await apiCall('GET', '/api/auth/me');
            localStorage.setItem('ferwafa_role', me.role);
            localStorage.setItem('ferwafa_entity_id', me.entityId);
            if (me.role === 'TEAM') window.location.href = '/team/dashboard';
            else if (me.role === 'ADMIN') window.location.href = '/admin/dashboard';
            else clearAuthStorage();
        } catch (_) {
            clearAuthStorage();
        }
    }
});
