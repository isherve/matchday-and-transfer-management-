async function handleLogin(username, password, role) {
    try {
        const data = await apiCall('POST', '/api/auth/login', { username, password });
        setToken(data.token);
        localStorage.setItem('ferwafa_role', data.role);
        localStorage.setItem('ferwafa_entity_id', data.entityId);
        // Use role returned by server (not the form tab label)
        if (data.role === 'ADMIN') {
            window.location.href = '/admin/dashboard';
        } else if (data.role === 'TEAM') {
            window.location.href = '/team/dashboard';
        } else {
            throw new Error('Invalid role for web login');
        }
    } catch (e) {
        throw e;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const adminForm = document.getElementById('adminLoginForm');
    if (adminForm) {
        adminForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const errEl = document.getElementById('adminError');
            errEl.classList.add('d-none');
            try {
                await handleLogin(
                    document.getElementById('adminUsername').value,
                    document.getElementById('adminPassword').value,
                    'ADMIN'
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
            errEl.classList.add('d-none');
            try {
                await handleLogin(
                    document.getElementById('teamUsername').value,
                    document.getElementById('teamPassword').value,
                    'TEAM'
                );
            } catch (err) {
                errEl.textContent = err.message || 'Login failed';
                errEl.classList.remove('d-none');
            }
        });
    }
});
