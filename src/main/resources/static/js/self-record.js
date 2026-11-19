
import { protocol, ApiError } from './protocol.js';


async function api(callback) {
    try {
        return await callback();
    } catch (error) {
        let message = 'Ошибка связи с сервером';

        if (error instanceof ApiError) {
            message = {
                400: 'Некорректный запрос',
                401: 'Не авторизован',
                403: 'Недостаточно прав',
                404: 'Ресурс не найден',
                409: 'Конфликт данных'
            }[error.status] ?? `Ошибка сервера: ${error.status}`;
        }

        alert(message);
        console.error(error);
        return null;
    }
}

function $(id) {
    return document.getElementById(id);
}

function toArray(data, ...keys) {
    if (Array.isArray(data))
        return data;

    if (!data || typeof data !== 'object')
        return [];

    for (const key of keys) {
        if (Array.isArray(data[key]))
            return data[key];
    }

    return [];
}

function fullName(user) {
    return [
        user?.lastName,
        user?.firstName,
        user?.patronymicName
    ].filter(Boolean).join(' ');
}

function formatAppointmentTime(value) {
    const date = new Date(value);

    if (Number.isNaN(date.getTime()))
        return 'Некорректная дата';

    const datePart = new Intl.DateTimeFormat('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    }).format(date);

    const timePart = new Intl.DateTimeFormat('ru-RU', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    }).format(date);

    return `${datePart}/${timePart}`;
}


async function loadRecord(patientId) {
    const data = await api(() =>
        protocol.entryRecord(patientId)
    );

    if (data === null)
        return;

    const entries = toArray(data, 'entries', 'data');
    const list = $('recordList');

    list.replaceChildren();

    if (entries.length === 0) {
        const li = document.createElement('li');
        li.textContent = 'Записей пока нет';
        list.append(li);
        return;
    }

    entries.forEach(entry => {
        const li = document.createElement('li');

        const doctor = entry.doctor ?? {};
        const doctorUser = doctor.user ?? {};
        const specialization = doctor.specialization ?? {};

        const h2 = document.createElement('h2');

        h2.textContent =
            `${specialization.specializationName ?? 'Специальность не указана'} - ${fullName(doctorUser)}`;

        const h3 = document.createElement('h3');

        h3.textContent =
            `[${entry.appointmentId}] ${formatAppointmentTime(entry.appointedTime)}`;

        const p = document.createElement('p');

        p.textContent = entry.content ?? '';

        li.append(h2, h3, p);
        list.append(li);
    });
}


const patientId = Number(
    document.getElementById('patientId').value
);

if (!patientId) {
    alert('Не удалось определить ID пациента');
} else {
    loadRecord(patientId);
}