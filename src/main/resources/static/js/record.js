document.addEventListener('DOMContentLoaded', () => {
    const page = document.querySelector('[data-record-role]');
    if (!page) return;
    const role = page.dataset.recordRole;
    const notice = document.querySelector('#record-notice');
    const list = document.querySelector('#record-list');
    const request = async (path, payload) => {
        try { return await HospitalApi.post(path, payload); }
        catch (error) { HospitalApi.showNotice(notice, error.message, 'error'); throw error; }
    };

    async function loadRecords(patientId) {
        const records = await request('/api/appointment/entry/record', { patientId: Number(patientId) });
        list.replaceChildren();
        if (!records.length) { list.innerHTML = '<div class="empty">В медицинской карте пока нет записей</div>'; return; }
        records.sort((a, b) => new Date(b.appointedTime) - new Date(a.appointedTime));
        records.forEach((record) => {
            const doctor = record.doctor?.user || {};
            const specialization = HospitalApi.specializationDetails(record.doctor?.specialization).name;
            const row = document.createElement('article'); row.className = 'item';
            const content = document.createElement('div');
            const title = document.createElement('strong'); title.textContent = formatDate(record.appointedTime);
            const meta = document.createElement('p'); meta.className = 'muted';
            meta.textContent = `${[doctor.lastName, doctor.firstName, doctor.patronymicName].filter(Boolean).join(' ')}${specialization ? ' · ' + specialization : ''} · приём №${record.appointmentId}`;
            const body = document.createElement('p'); body.style.whiteSpace = 'pre-wrap'; body.textContent = record.content || '';
            content.append(title, meta, body); row.append(content); list.append(row);
        });
    }

    async function loadPatientOptions() {
        const select = document.querySelector('#record-patient');
        const profile = await request('/api/user/profile');
        const currentUser = HospitalApi.getUserDetails(profile);
        let patients = [];
        if (currentUser.role === 'ADMIN') {
            const users = await request('/api/user/get');
            patients = users.map((item) => HospitalApi.getUserDetails(item)).filter((user) => user.role === 'PATIENT');
        } else if (currentUser.role === 'DOCTOR') {
            const appointments = await request('/api/appointment/get');
            const patientMap = new Map();
            appointments.filter((appointment) => appointment.patient?.user).forEach((appointment) => {
                const user = appointment.patient.user;
                patientMap.set(user.userId, user);
            });
            patients = Array.from(patientMap.values()).map((user) => ({ ...user, role: 'PATIENT' }));
        }
        select.replaceChildren();
        const emptyOption = document.createElement('option'); emptyOption.value = ''; emptyOption.textContent = 'Выберите пациента'; select.append(emptyOption);
        patients.forEach((patient) => {
            const option = document.createElement('option'); option.value = patient.userId ?? patient.id;
            option.textContent = `${[patient.lastName, patient.firstName, patient.patronymicName].filter(Boolean).join(' ')} · ID ${option.value}`;
            select.append(option);
        });
        if (!patients.length) HospitalApi.showNotice(notice, 'Не найдено пациентов, чьи медкарты доступны для просмотра', 'error');
    }

    if (role === 'PATIENT') {
        const start = async () => {
            const profile = await request('/api/user/profile');
            await loadRecords(HospitalApi.getUserDetails(profile).id);
        };
        document.querySelector('#refresh-record')?.addEventListener('click', () => start().catch(() => {}));
        start().catch(() => {});
    } else {
        const refresh = () => loadPatientOptions().catch(() => {});
        document.querySelector('#refresh-record-patients')?.addEventListener('click', refresh);
        document.querySelector('#record-search-form')?.addEventListener('submit', (event) => {
            event.preventDefault(); const id = document.querySelector('#record-patient').value;
            if (id) loadRecords(id).catch(() => {});
        });
        refresh();
    }

    function formatDate(value) { return value ? new Date(value).toLocaleString('ru-RU') : 'Дата не указана'; }
});
