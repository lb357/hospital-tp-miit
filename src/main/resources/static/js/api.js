(() => {
    async function post(path, payload) {
        const options = {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }
        };
        if (payload !== undefined) options.body = JSON.stringify(payload);

        const response = await fetch(path, options);
        const raw = await response.text();
        let data = null;
        if (raw) {
            try { data = JSON.parse(raw); } catch { data = raw; }
        }
        if (!response.ok) {
            const message = data && typeof data === 'object' && data.message
                ? data.message
                : (response.status === 403 ? 'Недостаточно прав или сессия завершена' : `Ошибка запроса (${response.status})`);
            const error = new Error(message);
            error.status = response.status;
            error.data = data;
            throw error;
        }
        return data;
    }

    function showNotice(target, message, type = 'success') {
        if (!target) return;
        target.textContent = message;
        target.className = `notice visible ${type}`;
    }

    function clearNotice(target) {
        if (!target) return;
        target.textContent = '';
        target.className = 'notice';
    }

    function getUserDetails(value) {
        const user = value?.user || value || {};
        return {
            id: user.userId ?? user.id ?? '',
            lastName: user.lastName ?? '',
            firstName: user.firstName ?? '',
            patronymicName: user.patronymicName ?? '',
            gender: value?.gender,
            specialization: value?.specialization ?? null,
            role: value?.gender !== undefined ? 'PATIENT' : (value?.specialization !== undefined ? 'DOCTOR' : 'ADMIN')
        };
    }

    function specializationDetails(value) {
        return {
            id: value?.specializationId ?? value?.SpecializationId ?? value?.id,
            name: value?.specializationName ?? value?.SpecializationName ?? value?.name ?? ''
        };
    }

    window.HospitalApi = { post, showNotice, clearNotice, getUserDetails, specializationDetails };
})();
