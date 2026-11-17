document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('#login-form');
    const notice = document.querySelector('#login-notice');
    if (!form) return;

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        HospitalApi.clearNotice(notice);
        const submit = form.querySelector('button[type="submit"]');
        submit.disabled = true;
        const formData = new FormData(form);
        try {
            await HospitalApi.post('/api/auth/login', {
                id: Number(formData.get('id')),
                password: String(formData.get('password'))
            });
            // Verify that AuthController actually established the cookie-backed session.
            await HospitalApi.post('/api/user/profile');
            window.location.assign('/profile');
        } catch (error) {
            HospitalApi.showNotice(notice, error.message, 'error');
            submit.disabled = false;
        }
    });
});
