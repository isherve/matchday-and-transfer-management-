async function handleLogin(username, password, button) {
    if (button) {
        button.disabled = true;
        button.dataset.originalText = button.textContent;
        button.textContent = 'Signing in…';
    }
    try {
        const data = await apiCall('POST', '/api/auth/login', {
            username: (username || '').trim(),
            password: password || ''
        });
        if (!data || !data.token) {
            throw new Error('Login failed — no token returned');
        }
        setToken(data.token);
        localStorage.setItem('ferwafa_role', data.role);
        localStorage.setItem('ferwafa_entity_id', String(data.entityId));
        if (data.role === 'ADMIN') {
            window.location.href = '/admin/dashboard';
        } else if (data.role === 'TEAM') {
            window.location.href = '/team/dashboard';
        } else {
            throw new Error('This account cannot use the web login. Use the Referee App instead.');
        }
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = button.dataset.originalText || button.textContent;
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const adminForm = document.getElementById('adminLoginForm');
    if (adminForm) {
        adminForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const errEl = document.getElementById('adminError');
            const btn = adminForm.querySelector('button[type="submit"]');
            errEl.classList.add('d-none');
            try {
                await handleLogin(
                    document.getElementById('adminUsername').value,
                    document.getElementById('adminPassword').value,
                    btn
                );
            } catch (err) {
                errEl.textContent = err.message || 'Login failed';
                errEl.classList.remove('d-none');
            }
        });
    }

    const teamForm = document.getElementById('teamLoginForm');
    if (teamForm) {
        teamForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const errEl = document.getElementById('teamError');
            const btn = teamForm.querySelector('button[type="submit"]');
            errEl.classList.add('d-none');
            try {
                await handleLogin(
                    document.getElementById('teamUsername').value,
                    document.getElementById('teamPassword').value,
                    btn
                );
            } catch (err) {
                errEl.textContent = err.message || 'Login failed';
                errEl.classList.remove('d-none');
            }
        });
    }
});
