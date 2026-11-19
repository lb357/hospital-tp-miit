
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

function fullName(user) {
    return [
        user.lastName,
        user.firstName,
        user.patronymicName
    ].filter(Boolean).join(' ');
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

function appointmentStatus(isCompleted) {
    return isCompleted
        ? 'Завершён'
        : 'Не завершён';
}

let appointments = [];

async function loadAppointments() {
    const data = await api(() =>
        protocol.appointmentGet()
    );

    if (data === null)
        return;

    appointments = toArray(
        data,
        'appointments',
        'data'
    );

    const list = $('appointmentsList');
    list.replaceChildren();

    appointments.forEach(appointment => {
        const li = document.createElement('li');

        const patient = appointment.patient?.user;
        const appointmentId = appointment.appointmentId;

        const time = formatAppointmentTime(
            appointment.appointedTime
        );

        const room = appointment.room ?? 'Не указан';

        const patientName = patient
            ? fullName(patient)
            : 'Пациент не назначен';

        const status = appointmentStatus(
            appointment.isCompleted
        );

        li.append(
            document.createTextNode(
                `[${time} - ${status}] Кабинет: ${room} / ${patientName}`
            )
        );

        const recordButton = document.createElement('button');

        recordButton.type = 'button';
        recordButton.textContent = 'Открыть медкарту пациента';

        recordButton.disabled = !patient?.userId;

        recordButton.addEventListener('click', () => {
            if (!patient?.userId)
                return;

            window.location.href =
                `/record?patientId=${encodeURIComponent(patient.userId)}`;
        });

        li.append(recordButton);

        const entryButton = document.createElement('button');

        entryButton.type = 'button';
        entryButton.textContent = 'Оставить запись в медкарте';

        entryButton.disabled = !appointmentId || !patient?.userId;

        entryButton.addEventListener('click', () => {
            if (!appointmentId)
                return;

            window.location.href =
                `/entry?appointmentId=${encodeURIComponent(appointmentId)}`;
        });

        li.append(entryButton);

        list.append(li);
    });
}



loadAppointments();
