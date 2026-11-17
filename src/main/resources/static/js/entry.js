document.addEventListener('DOMContentLoaded', () => {
    if (!document.querySelector('[data-entry-page]')) return;
    const notice = document.querySelector('#entry-notice');
    const select = document.querySelector('#entry-appointment');
    const content = document.querySelector('#entry-content');
    const save = document.querySelector('#entry-save');
    let mode = 'create';

    async function loadAppointments() {
        try {
            const appointments = await HospitalApi.post('/api/appointment/get');
            select.replaceChildren();
            const empty = document.createElement('option'); empty.value = ''; empty.textContent = 'Выберите приём с пациентом'; select.append(empty);
            appointments.filter((item) => item.patient).forEach((item) => {
                const patient = item.patient.user || {};
                const option = document.createElement('option'); option.value = item.appointmentId;
                option.textContent = `№${item.appointmentId} · ${new Date(item.appointedTime).toLocaleString('ru-RU')} · ${[patient.lastName, patient.firstName].filter(Boolean).join(' ')}`;
                select.append(option);
            });
            if (!select.options.length || select.options.length === 1) HospitalApi.showNotice(notice, 'В расписании нет приёмов с пациентами', 'error');
        } catch (error) { HospitalApi.showNotice(notice, error.message, 'error'); }
    }

    select.addEventListener('change', async () => {
        content.value = ''; save.disabled = !select.value;
        if (!select.value) return;
        try {
            const entry = await HospitalApi.post('/api/appointment/entry/get', { appointmentId: Number(select.value) });
            content.value = entry.content || ''; mode = 'update'; save.textContent = 'Обновить запись';
        } catch (error) {
            if (error.status === 404) { mode = 'create'; save.textContent = 'Создать запись'; }
            else HospitalApi.showNotice(notice, error.message, 'error');
        }
    });

    document.querySelector('#entry-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const text = content.value.trim();
        if (!select.value || !text) { HospitalApi.showNotice(notice, 'Выберите приём и заполните текст записи', 'error'); return; }
        try {
            await HospitalApi.post(`/api/appointment/entry/${mode}`, { appointmentId: Number(select.value), content: text });
            HospitalApi.showNotice(notice, mode === 'create' ? 'Запись создана' : 'Запись обновлена');
            mode = 'update'; save.textContent = 'Обновить запись';
        } catch (error) { HospitalApi.showNotice(notice, error.message, 'error'); }
    });

    loadAppointments();
});
