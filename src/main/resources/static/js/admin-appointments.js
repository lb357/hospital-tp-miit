
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

function createOption(value, label) {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = label;
    return option;
}

function fillSelect(select, items, getId, getLabel) {
    select.replaceChildren(createOption('', '-- Выберите --'));

    items.forEach(item => {
        select.append(
            createOption(getId(item), getLabel(item))
        );
    });
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

function showList(element, items, getText) {
    element.replaceChildren();

    items.forEach(item => {
        const li = document.createElement('li');
        li.textContent = getText(item);
        element.append(li);
    });
}


let appointments = [];
let specializations = [];
let doctors = [];
let patients = [];
let checkups = [];

async function loadSpecializations() {
    const data = await api(() =>
        protocol.specializationGet()
    );

    if (data === null)
        return false;

    specializations = toArray(
        data,
        'specializations',
        'data'
    );

    const select = $('checkupSpecializations');
    select.replaceChildren();

    specializations.forEach(item => {
        select.append(
            createOption(
                item.specializationId,
                item.specializationName
            )
        );
    });

    return true;
}

async function loadDoctors() {
    const result = [];

    for (const specialization of specializations) {
        const data = await api(() =>
            protocol.specialists(
                specialization.specializationId
            )
        );

        if (data === null)
            continue;

        result.push(
            ...toArray(data, 'doctors', 'data')
        );
    }

    const unique = new Map();

    result.forEach(doctor => {
        const user = doctor.user ?? doctor;

        unique.set(user.userId, doctor);
    });

    doctors = [...unique.values()];

    fillSelect(
        $('appointmentDoctor'),
        doctors,
        doctor => (doctor.user ?? doctor).userId,
        doctor => {
            const user = doctor.user ?? doctor;

            return `${user.userId} - ${fullName(user)}`;
        }
    );
}


async function loadPatients() {
    const data = await api(() =>
        protocol.userGet()
    );

    if (data === null)
        return;

    patients = toArray(data, 'users', 'data', 'items')
        .map(item => ({
            ...item.user,
            role: item.role,
            gender: item.gender
        }))
        .filter(user =>
            (user.role ?? user.userRole ?? '').toUpperCase() === 'PATIENT'
            || user.gender !== undefined
        );

    fillSelect(
        $('checkupPatient'),
        patients,
        patient => patient.userId,
        patient => `${patient.userId} - ${fullName(patient)}`
    );
}

function formatDateTime(value) {
    if (!value)
        return 'Дата не указана';

    const date = new Date(value);

    if (Number.isNaN(date.getTime()))
        return 'Некорректная дата';

    return new Intl.DateTimeFormat('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    }).format(date).replace(',', '/');
}

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

    showList(
        $('appointmentsList'),
        appointments,
        appointment => {
            const doctor = appointment.doctor?.user ?? {};
            const patient = appointment.patient?.user ?? {};

            const status = appointment.isCompleted
                ? 'Завершён'
                : 'Не завершён';

            return `[${appointment.appointmentId}] ${formatDateTime(appointment.appointedTime)} / Кабинет ${appointment.room} / Пациент: ${fullName(patient)} / Врач: ${fullName(doctor)} / ${status}`;
        }
    );

    fillSelect(
        $('appointmentSelect'),
        appointments,
        item => item.appointmentId,
        item => {
            const doctor = item.doctor?.user ?? {};
            const patient = item.patient?.user ?? {};

            return `${item.appointmentId} - ${fullName(patient)} - ${fullName(doctor)} - ${formatDateTime(item.appointedTime)}`;
        }
    );
}

$('appointmentDeleteForm').addEventListener('submit', async event => {
    event.preventDefault();

    const appointmentId = Number(
        $('appointmentSelect').value
    );

    if (!appointmentId)
        return;

    if (!confirm('Удалить приём?'))
        return;

    const result = await api(() =>
        protocol.appointmentDelete(appointmentId)
    );

    if (result === null)
        return;

    await loadAppointments();
});

$('appointmentCreateForm').addEventListener('submit', async event => {
    event.preventDefault();

    const doctorId = Number($('appointmentDoctor').value);
    const room = $('appointmentRoom').value;
    const timeValue = $('appointmentTime').value;

    if (!doctorId || !timeValue)
        return;

    const appointedTime = new Date(timeValue).toISOString();

    const result = await api(() =>
        protocol.appointmentCreate(
            doctorId,
            room,
            appointedTime
        )
    );

    if (result === null)
        return;

    alert('Приём добавлен');
    $('appointmentCreateForm').reset();

    await loadAppointments();
});


async function loadCheckups() {
    checkups = [];

    for (const patient of patients) {
        const data = await api(() =>
            protocol.checkupGet(patient.userId)
        );

        if (data === null)
            continue;

        checkups.push(
            ...toArray(data, 'checkups', 'data')
        );
    }

    showList(
        $('checkupsList'),
        checkups,
        checkup => {
            const patient = checkup.patient?.user ?? {};

            const names = (
                checkup.specializationIds ?? []
            ).map(item => item.specializationName).join(', ');

            const status = checkup.isCompleted
                ? 'Пройден'
                : 'Не пройден';

            return `[${checkup.checkupId} - ${status}] ${fullName(patient)} / ${names}`;
        }
    );

    fillSelect(
        $('checkupSelect'),
        checkups,
        item => item.checkupId,
        item => {
            const patient = item.patient?.user ?? {};

            return `${item.checkupId} - ${fullName(patient)}`;
        }
    );
}

$('checkupDeleteForm').addEventListener('submit', async event => {
    event.preventDefault();

    const checkupId = Number($('checkupSelect').value);

    if (!checkupId)
        return;

    if (!confirm('Удалить медосмотр?'))
        return;

    const result = await api(() =>
        protocol.checkupDelete(checkupId)
    );

    if (result === null)
        return;

    await loadCheckups();
});

$('checkupCreateForm').addEventListener('submit', async event => {
    event.preventDefault();

    const patientId = Number($('checkupPatient').value);

    const specializationIds = [
        ...$('checkupSpecializations').selectedOptions
    ].map(option => Number(option.value));

    if (!patientId || specializationIds.length === 0)
        return;

    const result = await api(() =>
        protocol.checkupCreate(
            patientId,
            specializationIds
        )
    );

    if (result === null)
        return;

    alert('Медосмотр назначен');
    $('checkupCreateForm').reset();

    await loadCheckups();
});

async function init() {
    const loaded = await loadSpecializations();

    if (!loaded)
        return;

    await loadDoctors();
    await loadPatients();
    await loadAppointments();
    await loadCheckups();
}

init();
