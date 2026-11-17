document.addEventListener('DOMContentLoaded', () => {
    const logoutButton = document.querySelector('[data-logout]');
    if (logoutButton) {
        logoutButton.addEventListener('click', async () => {
            logoutButton.disabled = true;
            try {
                await HospitalApi.post('/api/auth/logout', {});
                try {
                    await HospitalApi.post('/api/user/profile');
                    throw new Error('Сервер не завершил сессию: профиль всё ещё доступен');
                } catch (error) {
                    if (error.status !== 403) throw error;
                }
                window.location.assign('/login');
            } catch (error) {
                window.alert(error.message + '. Возможно, AuthController ещё не подключён.');
                logoutButton.disabled = false;
            }
        });
    }
});
