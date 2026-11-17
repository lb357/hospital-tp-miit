document.addEventListener('DOMContentLoaded', async () => {
    if (!document.querySelector('[data-profile-page]')) return;
    const notice = document.querySelector('#profile-notice');
    try {
        const response = await HospitalApi.post('/api/user/profile');
        const details = HospitalApi.getUserDetails(response);
        document.querySelector('#profile-name').textContent = [details.lastName, details.firstName, details.patronymicName].filter(Boolean).join(' ') || 'Пользователь';
        const lines = [`Идентификационный номер: ${details.id}`, `Роль: ${roleLabel(details.role)}`];
        if (details.specialization) {
            const specialization = HospitalApi.specializationDetails(details.specialization);
            if (specialization.name) lines.push(`Специальность: ${specialization.name}`);
        }
        if (details.gender !== undefined) lines.push(`Пол: ${details.gender ? 'мужской' : 'женский'}`);
        document.querySelector('#profile-details').textContent = lines.join(' · ');
    } catch (error) {
        HospitalApi.showNotice(notice, error.message, 'error');
        document.querySelector('#profile-name').textContent = 'Не удалось загрузить профиль';
    }
});

function roleLabel(role) {
    return ({ ADMIN: 'Администратор', DOCTOR: 'Врач', PATIENT: 'Пациент' })[role] || role;
}
