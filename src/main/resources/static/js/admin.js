document.addEventListener('DOMContentLoaded', () => {
    if (!document.querySelector('[data-admin-page]')) return;

    const notice = document.querySelector('#admin-notice');
    const el = (id) => document.getElementById(id);
    let users = [];
    let specializations = [];
    let appointments = [];

    const report = (message, type = 'success') => HospitalApi.showNotice(notice, message, type);
    const request = async (path, payload) => {
        try { return await HospitalApi.post(path, payload); }
        catch (error) { report(error.message, 'error'); throw error; }
    };
    const userName = (item) => {
        const u = HospitalApi.getUserDetails(item);
        return { ...u, title: [u.lastName, u.firstName, u.patronymicName].filter(Boolean).join(' ') || `Пользователь ${u.id}` };
    };
    const roleName = (role) => ({ ADMIN: 'Администратор', DOCTOR: 'Врач', PATIENT: 'Пациент' })[role] || role;
    const addOption = (select, value, text) => {
        const option = document.createElement('option'); option.value = value; option.textContent = text; select.append(option);
    };

    async function loadUsers() {
        users = await request('/api/user/get');
        const body = el('user-list'); body.replaceChildren();
        const patients = [], doctors = [];
        users.forEach((item) => {
            const u = userName(item);
            if (u.role === 'PATIENT') patients.push({ item, u });
            if (u.role === 'DOCTOR') doctors.push({ item, u });
            const row = document.createElement('tr');
            const idCell = document.createElement('td'); idCell.textContent = u.id;
            const nameCell = document.createElement('td'); nameCell.textContent = u.title;
            const roleCell = document.createElement('td'); roleCell.textContent = roleName(u.role);
            const actionsCell = document.createElement('td');
            const edit = document.createElement('button'); edit.className = 'secondary'; edit.textContent = 'Изменить';
            edit.addEventListener('click', () => editUser(u));
            const remove = document.createElement('button'); remove.className = 'danger'; remove.textContent = 'Удалить'; remove.style.marginLeft = '6px';
            remove.addEventListener('click', async () => {
                if (!window.confirm(`Удалить пользователя ${u.title} (ID ${u.id})?`)) return;
                await request('/api/user/delete', { userId: Number(u.id) });
                report('Пользователь удалён'); await loadUsers();
            });
            actionsCell.append(edit, remove); row.append(idCell, nameCell, roleCell, actionsCell); body.append(row);
        });
        if (!users.length) body.innerHTML = '<tr><td colspan="4" class="muted">Пользователей пока нет</td></tr>';
        fillSelect(el('checkup-patient'), patients, 'Выберите пациента');
        fillSelect(el('record-patient'), patients, 'Выберите пациента');
        fillSelect(el('slot-doctor'), doctors, 'Выберите врача');
    }

    function fillSelect(select, entries, placeholder) {
        select.replaceChildren(); addOption(select, '', placeholder);
        entries.forEach(({ u }) => addOption(select, u.id, `${u.title} · ID ${u.id}`));
    }

    async function editUser(user) {
        const lastName = window.prompt('Фамилия', user.lastName);
        if (lastName === null) return;
        const firstName = window.prompt('Имя', user.firstName);
        if (firstName === null) return;
        const patronymicName = window.prompt('Отчество', user.patronymicName || '');
        if (patronymicName === null) return;
        await request('/api/user/update', { userId: Number(user.id), lastName, firstName, patronymicName });
        report('Данные пользователя обновлены'); await loadUsers();
    }

    async function loadSpecializations() {
        specializations = await request('/api/user/specialization/get');
        const list = el('specialization-list'); list.replaceChildren();
        el('signup-specialization').replaceChildren(); addOption(el('signup-specialization'), '', 'Выберите специальность');
        el('checkup-specializations').replaceChildren();
        specializations.forEach((item) => {
            const s = HospitalApi.specializationDetails(item);
            addOption(el('signup-specialization'), s.id, s.name);
            addOption(el('checkup-specializations'), s.id, s.name);
            const row = document.createElement('div'); row.className = 'item';
            const title = document.createElement('div'); title.textContent = `${s.name} · ID ${s.id}`;
            const button = document.createElement('button'); button.className = 'danger'; button.textContent = 'Удалить';
            button.addEventListener('click', async () => {
                if (!window.confirm(`Удалить специальность «${s.name}»?`)) return;
                await request('/api/user/specialization/update', { specializationId: Number(s.id) });
                report('Специальность удалена'); await loadSpecializations();
            });
            row.append(title, button); list.append(row);
        });
        if (!specializations.length) list.innerHTML = '<div class="empty">Специальностей пока нет</div>';
    }

    async function loadSlots() {
        appointments = await request('/api/appointment/get');
        const list = el('slot-list'); list.replaceChildren();
        appointments.forEach((appointment) => {
            const doctor = appointment.doctor?.user || {};
            const fullName = [doctor.lastName, doctor.firstName, doctor.patronymicName].filter(Boolean).join(' ');
            const row = document.createElement('div'); row.className = 'item';
            const details = document.createElement('div');
            details.innerHTML = `<strong>${escapeHtml(fullName || 'Врач')}</strong><p class="muted">${escapeHtml(formatDate(appointment.appointedTime))} · кабинет ${escapeHtml(appointment.room || '—')} · ${appointment.patient ? 'занят' : 'свободен'} · ID ${appointment.appointmentId}</p>`;
            const button = document.createElement('button'); button.className = 'danger'; button.textContent = 'Удалить';
            button.addEventListener('click', async () => {
                if (!window.confirm('Удалить этот слот/приём?')) return;
                await request('/api/appointment/delete', { appointmentId: Number(appointment.appointmentId) });
                report('Запись удалена'); await loadSlots();
            });
            row.append(details, button); list.append(row);
        });
        if (!appointments.length) list.innerHTML = '<div class="empty">Расписание пока пусто</div>';
    }

    async function loadCheckups() {
        const checkups = await request('/api/checkup/get', {});
        const list = el('checkup-list'); list.replaceChildren();
        checkups.forEach((checkup) => {
            const patient = HospitalApi.getUserDetails(checkup.patient);
            const patientName = [patient.lastName, patient.firstName, patient.patronymicName].filter(Boolean).join(' ');
            const specs = (checkup.specializations || []).map((x) => HospitalApi.specializationDetails(x).name).join(', ');
            const row = document.createElement('div'); row.className = 'item';
            const details = document.createElement('div'); details.innerHTML = `<strong>${escapeHtml(patientName)} · ID ${patient.id}</strong><p class="muted">${escapeHtml(specs)} · назначение №${checkup.checkupId}</p>`;
            const button = document.createElement('button'); button.className = 'danger'; button.textContent = 'Удалить';
            button.addEventListener('click', async () => {
                if (!window.confirm('Удалить назначение медосмотра?')) return;
                await request('/api/checkup/delete', { checkupId: Number(checkup.checkupId) });
                report('Назначение медосмотра удалено'); await loadCheckups();
            });
            row.append(details, button); list.append(row);
        });
        if (!checkups.length) list.innerHTML = '<div class="empty">Назначенных медосмотров пока нет</div>';
    }

    el('refresh-users').addEventListener('click', () => loadUsers().catch(() => {}));
    el('refresh-specializations').addEventListener('click', () => loadSpecializations().catch(() => {}));
    el('refresh-slots').addEventListener('click', () => loadSlots().catch(() => {}));
    el('refresh-checkups').addEventListener('click', () => loadCheckups().catch(() => {}));

    el('signup-role').addEventListener('change', () => {
        const role = el('signup-role').value;
        el('gender-field').classList.toggle('hidden', role !== 'PATIENT');
        el('signup-specialization-field').classList.toggle('hidden', role !== 'DOCTOR');
    });
    el('signup-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        const role = String(data.get('role'));
        const payload = {
            role, lastName: data.get('lastName'), firstName: data.get('firstName'),
            patronymicName: data.get('patronymicName'), password: data.get('password')
        };
        if (role === 'PATIENT') payload.gender = data.get('gender') === 'true';
        if (role === 'DOCTOR') payload.specializationId = Number(data.get('specializationId'));
        try {
            const created = await request('/api/auth/signup', payload);
            const createdId = created?.userId ?? created?.id ?? created?.user?.userId ?? created?.user?.id;
            if (createdId == null) {
                HospitalApi.showNotice(el('signup-result'), 'AuthController не вернул идентификатор созданного пользователя; проверьте его реализацию и контракт.', 'error');
                return;
            }
            HospitalApi.showNotice(el('signup-result'), `Учётная запись создана. Идентификатор: ${createdId}`);
            event.currentTarget.reset(); el('signup-role').dispatchEvent(new Event('change'));
            await loadUsers();
        } catch (_) { /* The shared notice already displays the error. */ }
    });

    el('specialization-form').addEventListener('submit', async (event) => {
        event.preventDefault(); const name = new FormData(event.currentTarget).get('name');
        try { await request('/api/user/specialization/create', { name }); report('Специальность добавлена'); event.currentTarget.reset(); await loadSpecializations(); }
        catch (_) {}
    });

    el('slot-form').addEventListener('submit', async (event) => {
        event.preventDefault(); const data = new FormData(event.currentTarget);
        try {
            await request('/api/appointment/create', {
                doctorId: Number(data.get('doctorId')), room: data.get('room'), appointedTime: data.get('appointedTime')
            });
            report('Слот приёма создан'); event.currentTarget.reset(); await loadSlots();
        } catch (_) {}
    });

    el('checkup-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const specializationIds = Array.from(el('checkup-specializations').selectedOptions).map((option) => Number(option.value));
        try {
            await request('/api/checkup/create', { patientId: Number(el('checkup-patient').value), specializationIds });
            report('Медосмотр назначен'); event.currentTarget.reset(); await loadCheckups();
        } catch (_) {}
    });

    el('record-search-form').addEventListener('submit', async (event) => {
        event.preventDefault(); const patientId = Number(el('record-patient').value);
        try {
            const records = await request('/api/appointment/entry/record', { patientId });
            renderRecords(el('admin-record-results'), records);
        } catch (_) {}
    });

    Promise.all([loadUsers(), loadSpecializations(), loadSlots(), loadCheckups()]).catch(() => {});

    function renderRecords(container, records) {
        container.replaceChildren();
        if (!records.length) { container.innerHTML = '<div class="empty">В медкарте пока нет записей</div>'; return; }
        records.forEach((record) => {
            const doctor = record.doctor?.user || {};
            const spec = HospitalApi.specializationDetails(record.doctor?.specialization).name;
            const row = document.createElement('div'); row.className = 'item';
            const text = document.createElement('div');
            text.innerHTML = `<strong>${escapeHtml(formatDate(record.appointedTime))} · ${escapeHtml([doctor.lastName, doctor.firstName].filter(Boolean).join(' '))}</strong><p class="muted">${escapeHtml(spec)}</p><p>${escapeHtml(record.content || '')}</p>`;
            row.append(text); container.append(row);
        });
    }

    function formatDate(value) { return value ? new Date(value).toLocaleString('ru-RU') : 'Дата не указана'; }
    function escapeHtml(value) { return String(value ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]); }
});
