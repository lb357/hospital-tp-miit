document.addEventListener('DOMContentLoaded', () => {
    const page = document.querySelector('[data-appointments-role]');
    if (!page) return;
    const role = page.dataset.appointmentsRole;
    const notice = document.querySelector('#appointments-notice');
    const list = document.querySelector('#appointment-list');

    const request = async (path, payload) => {
        try { return await HospitalApi.post(path, payload); }
        catch (error) { HospitalApi.showNotice(notice, error.message, 'error'); throw error; }
    };

    async function loadAppointments() {
        const appointments = await request('/api/appointment/get');
        list.replaceChildren();
        if (!appointments.length) {
            list.innerHTML = '<div class="empty">Приёмов пока нет</div>';
            return;
        }
        appointments.sort((a, b) => new Date(a.appointedTime) - new Date(b.appointedTime));
        appointments.forEach((appointment) => {
            const row = document.createElement('div'); row.className = 'item';
            const doctor = appointment.doctor?.user || {};
            const patient = appointment.patient?.user || {};
            const spec = HospitalApi.specializationDetails(appointment.doctor?.specialization).name;
            const details = document.createElement('div');
            const person = role === 'DOCTOR'
                ? `${[patient.lastName, patient.firstName, patient.patronymicName].filter(Boolean).join(' ') || 'Свободный слот'}`
                : `${[doctor.lastName, doctor.firstName, doctor.patronymicName].filter(Boolean).join(' ') || 'Врач'}`;
            const title = document.createElement('strong'); title.textContent = `${formatDate(appointment.appointedTime)} · ${person}`;
            const meta = document.createElement('p'); meta.className = 'muted';
            meta.textContent = `${spec ? spec + ' · ' : ''}Кабинет ${appointment.room || '—'} · ${appointment.patient ? 'занят' : 'свободен'} · ID ${appointment.appointmentId}`;
            details.append(title, meta); row.append(details);

            if (role === 'DOCTOR') {
                const button = document.createElement('button'); button.textContent = 'Запись в медкарту';
                button.disabled = !appointment.patient;
                button.addEventListener('click', () => openEntry(appointment)); row.append(button);
            }
            if (role === 'ADMIN') {
                const button = document.createElement('button'); button.className = 'danger'; button.textContent = 'Удалить';
                button.addEventListener('click', async () => {
                    if (!window.confirm('Удалить этот слот/приём?')) return;
                    try { await request('/api/appointment/delete', { appointmentId: Number(appointment.appointmentId) }); await loadAppointments(); }
                    catch (_) {}
                }); row.append(button);
            }
            list.append(row);
        });
    }

    async function loadPatientCheckups() {
        const target = document.querySelector('#patient-checkup-list');
        if (!target) return;
        const checkups = await request('/api/checkup/get', {});
        target.replaceChildren();
        if (!checkups.length) { target.innerHTML = '<div class="empty">Назначенных медосмотров пока нет</div>'; return; }
        checkups.forEach((checkup) => {
            const names = (checkup.specializations || []).map((item) => HospitalApi.specializationDetails(item).name).join(', ');
            const row = document.createElement('div'); row.className = 'item';
            const content = document.createElement('div');
            const title = document.createElement('strong'); title.textContent = `Медосмотр №${checkup.checkupId}`;
            const subtitle = document.createElement('p'); subtitle.className = 'muted'; subtitle.textContent = names || 'Специальности не указаны';
            content.append(title, subtitle); row.append(content); target.append(row);
        });
    }

    async function openEntry(appointment) {
        const appointmentId = Number(appointment.appointmentId);
        const idField = document.querySelector('#entry-appointment-id');
        const contentField = document.querySelector('#entry-content');
        const button = document.querySelector('#save-entry');
        const hint = document.querySelector('#selected-appointment-hint');
        idField.value = appointmentId;
        button.disabled = false;
        const patient = appointment.patient?.user || {};
        hint.textContent = `Приём №${appointmentId} · ${[patient.lastName, patient.firstName, patient.patronymicName].filter(Boolean).join(' ')} · ${formatDate(appointment.appointedTime)}`;
        contentField.value = '';
        contentField.focus();
        try {
            const entry = await HospitalApi.post('/api/appointment/entry/get', { appointmentId });
            contentField.value = entry.content || '';
            button.dataset.mode = 'update';
            button.textContent = 'Обновить запись';
        } catch (error) {
            if (error.status === 404) {
                button.dataset.mode = 'create';
                button.textContent = 'Создать запись';
            } else {
                HospitalApi.showNotice(notice, error.message, 'error');
            }
        }
    }

    const entryForm = document.querySelector('#entry-form');
    if (entryForm) {
        entryForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const appointmentId = Number(document.querySelector('#entry-appointment-id').value);
            const content = document.querySelector('#entry-content').value.trim();
            if (!appointmentId || !content) { HospitalApi.showNotice(notice, 'Выберите приём и заполните содержание осмотра', 'error'); return; }
            const mode = document.querySelector('#save-entry').dataset.mode || 'create';
            try {
                await request(`/api/appointment/entry/${mode}`, { appointmentId, content });
                HospitalApi.showNotice(notice, mode === 'create' ? 'Запись в медкарте создана' : 'Запись обновлена');
                document.querySelector('#save-entry').textContent = 'Обновить запись';
                document.querySelector('#save-entry').dataset.mode = 'update';
            } catch (_) {}
        });
        document.querySelector('#clear-entry').addEventListener('click', () => {
            entryForm.reset(); document.querySelector('#entry-appointment-id').value = '';
            document.querySelector('#save-entry').disabled = true;
            document.querySelector('#selected-appointment-hint').textContent = 'Выберите приём из списка выше.';
        });
    }

    document.querySelector('#refresh-appointments')?.addEventListener('click', () => loadAppointments().catch(() => {}));
    document.querySelector('#refresh-checkups')?.addEventListener('click', () => loadPatientCheckups().catch(() => {}));
    loadAppointments().catch(() => {});
    if (role === 'PATIENT') loadPatientCheckups().catch(() => {});

    function formatDate(value) { return value ? new Date(value).toLocaleString('ru-RU') : 'Дата не указана'; }
});
