const API_BASE = '';

function getToken() {
    return localStorage.getItem('ferwafa_token') || getCookie('ferwafa_token');
}

function setToken(token) {
    localStorage.setItem('ferwafa_token', token);
    // Also set a JS-readable cookie as fallback (server sets HttpOnly on login)
    document.cookie = `ferwafa_token=${encodeURIComponent(token)}; path=/; max-age=86400; SameSite=Lax`;
}

function getCookie(name) {
    const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return match ? decodeURIComponent(match[1]) : null;
}

async function logout() {
    try {
        await fetch(API_BASE + '/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch (_) { /* ignore */ }
    localStorage.removeItem('ferwafa_token');
    localStorage.removeItem('ferwafa_role');
    localStorage.removeItem('ferwafa_entity_id');
    document.cookie = 'ferwafa_token=; path=/; max-age=0';
    window.location.href = '/login';
}

async function apiCall(method, url, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const opts = { method, headers, credentials: 'include' };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(API_BASE + url, opts);
    if (res.status === 401 || res.status === 403) {
        if (url.startsWith('/api/') && !url.includes('/auth/login')) {
            const data = await res.json().catch(() => null);
            throw new Error(data?.message || (res.status === 403 ? 'Access denied' : 'Unauthorized'));
        }
    }
    if (res.status === 401) { await logout(); return; }
    const contentType = res.headers.get('content-type') || '';
    if (contentType.includes('application/pdf') || contentType.includes('octet-stream')) {
        if (!res.ok) throw new Error('Request failed');
        return res.blob();
    }
    const data = await res.json().catch(() => null);
    if (!res.ok) throw new Error(data?.message || data?.error || 'Request failed');
    return data;
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} position-fixed bottom-0 end-0 m-3`;
    toast.style.zIndex = 9999;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('error') === 'access_denied' || params.get('error') === 'login_required') {
        const box = document.getElementById('adminError') || document.getElementById('teamError');
        if (box) {
            box.textContent = params.get('error') === 'access_denied'
                ? 'Access denied for that page. Please log in with the correct account (Admin vs Team).'
                : 'Please log in to continue.';
            box.classList.remove('d-none');
        }
    }
    const token = getToken();
    const path = window.location.pathname;
    if (token && (path === '/login' || path === '/')) {
        const role = localStorage.getItem('ferwafa_role');
        window.location.href = role === 'TEAM' ? '/team/dashboard' : '/admin/dashboard';
    }
});
