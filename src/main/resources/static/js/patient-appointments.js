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

function createOption(value, label) {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = label;
    return option;
}

function fillSelect(
    select,
    items,
    getId,
    getLabel,
    placeholder = '-- Выберите --'
) {
    select.replaceChildren(
        createOption('', placeholder)
    );

    items.forEach(item => {
        select.append(
            createOption(getId(item), getLabel(item))
        );
    });

    select.disabled = items.length === 0;
}

function fullName(user) {
    if (!user)
        return 'Не указан';

    return [
        user.lastName,
        user.firstName,
        user.patronymicName
    ].filter(Boolean).join(' ');
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
    }).format(date).replace(', ', '/');
}

let appointments = [];
let checkups = [];
let specializations = [];
let doctors = [];
let doctorAppointments = [];


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

    if (appointments.length === 0) {
        const li = document.createElement('li');
        li.textContent = 'У вас пока нет приёмов';
        list.append(li);
        return;
    }

    appointments.forEach(appointment => {
        const li = document.createElement('li');

        const doctor = appointment.doctor?.user;
        const appointmentId = appointment.appointmentId;

        const status = appointment.isCompleted
            ? '(Завершён)'
            : '(Не завершён)';

        li.append(
            document.createTextNode(
                `[${appointmentId} - ${formatDateTime(appointment.appointedTime)}] `
                + `Кабинет: ${appointment.room ?? 'Не указан'} / `
                + `Врач: ${fullName(doctor)} `
                + status + ' '
            )
        );

        const cancelButton = document.createElement('button');

        cancelButton.type = 'button';
        cancelButton.textContent = 'Отменить запись';

        cancelButton.disabled = appointment.isCompleted;

        cancelButton.addEventListener('click', async () => {
            if (!confirm('Вы действительно хотите отменить запись?'))
                return;

            const result = await api(() =>
                protocol.appointmentRemove(appointmentId)
            );

            if (result === null)
                return;

            if (result.success !== true) {
                alert('Не удалось отменить запись');
                return;
            }

            alert('Запись отменена');

            await loadAppointments();
        });

        li.append(cancelButton);
        list.append(li);
    });
}


async function loadCheckups() {
    const data = await api(() =>
        protocol.checkupGet()
    );

    if (data === null)
        return false;

    checkups = toArray(
        data,
        'checkups',
        'data'
    );

    const list = $('checkupsList');
    list.replaceChildren();

    if (checkups.length === 0) {
        const li = document.createElement('li');
        li.textContent = 'У вас пока нет назначенных медосмотров';
        list.append(li);
    }

    checkups.forEach(checkup => {
        const li = document.createElement('li');

        const names = (checkup.specializationIds ?? [])
            .map(item => item.specializationName)
            .filter(Boolean)
            .join(', ');

        const status = checkup.isCompleted
            ? 'Пройден'
            : 'Не пройден';

        li.textContent =
            `[№${checkup.checkupId}] `
            + `${names || 'Специальности не указаны'} / `
            + status;

        list.append(li);
    });

    fillDirections();

    return true;
}


function fillDirections() {
    const select = $('bookingDirection');

    select.replaceChildren(
        createOption('', '-- Выберите направление --'),
        createOption('SELF', 'Самостоятельное')
    );

    checkups.forEach(checkup => {
        select.append(
            createOption(
                `CHECKUP_${checkup.checkupId}`,
                `№${checkup.checkupId} Медосмотр`
            )
        );
    });
}

function onDirectionChange() {
    resetSpecializations();

    const direction = $('bookingDirection').value;

    if (!direction)
        return;

    if (direction === 'SELF') {
        showSpecializations(specializations);
        return;
    }

    if (direction.startsWith('CHECKUP_')) {
        const checkupId = Number(
            direction.replace('CHECKUP_', '')
        );

        const checkup = checkups.find(
            item => item.checkupId === checkupId
        );

        if (!checkup)
            return;

        showSpecializations(
            checkup.specializationIds ?? []
        );
    }
}

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

    return true;
}

function showSpecializations(items) {
    fillSelect(
        $('bookingSpecialization'),
        items,
        item => item.specializationId,
        item => item.specializationName,
        '-- Выберите специальность --'
    );
}

function resetSpecializations() {
    fillSelect(
        $('bookingSpecialization'),
        [],
        item => item.specializationId,
        item => item.specializationName,
        '-- Сначала выберите направление --'
    );

    resetDoctors();
}

function resetDoctors() {
    doctors = [];

    fillSelect(
        $('bookingDoctor'),
        [],
        item => (item.user ?? item).userId,
        item => fullName(item.user ?? item),
        '-- Сначала выберите специальность --'
    );

    resetAppointments();
}

function resetAppointments() {
    doctorAppointments = [];

    fillSelect(
        $('bookingAppointment'),
        [],
        item => item.appointmentId,
        item => item.appointmentId,
        '-- Сначала выберите специалиста --'
    );

    $('bookingSubmit').disabled = true;
}

async function loadDoctors() {
    resetDoctors();

    const specializationId = Number(
        $('bookingSpecialization').value
    );

    if (!specializationId)
        return;

    const data = await api(() =>
        protocol.specialists(specializationId)
    );

    if (data === null)
        return;

    const result = toArray(
        data,
        'doctors',
        'data'
    );

    const unique = new Map();

    result.forEach(doctor => {
        const user = doctor.user ?? doctor;

        if (user.userId)
            unique.set(user.userId, doctor);
    });

    doctors = [...unique.values()];

    fillSelect(
        $('bookingDoctor'),
        doctors,
        doctor => (doctor.user ?? doctor).userId,
        doctor => {
            const user = doctor.user ?? doctor;

            return `${fullName(user)} (ID: ${user.userId})`;
        },
        '-- Выберите специалиста --'
    );
}


async function loadDoctorAppointments() {
    resetAppointments();

    const doctorId = Number(
        $('bookingDoctor').value
    );

    if (!doctorId)
        return;

    const data = await api(() =>
        protocol.appointmentGet(doctorId)
    );

    if (data === null)
        return;

    const result = toArray(
        data,
        'appointments',
        'data'
    );

    doctorAppointments = result.filter(
        appointment =>
            appointment.patient == null
            && !appointment.isCompleted
    );

    fillSelect(
        $('bookingAppointment'),
        doctorAppointments,
        appointment => appointment.appointmentId,
        appointment =>
            `${formatDateTime(appointment.appointedTime)} / `
            + `Кабинет: ${appointment.room ?? 'Не указан'}`
            + ` / ID: ${appointment.appointmentId}`,
        '-- Выберите приём --'
    );

    if (doctorAppointments.length === 0) {
        $('bookingAppointment').replaceChildren(
            createOption('', '-- Нет свободных приёмов --')
        );

        $('bookingAppointment').disabled = true;
    }
}


async function bookAppointment(event) {
    event.preventDefault();

    const direction = $('bookingDirection').value;

    const appointmentId = Number(
        $('bookingAppointment').value
    );

    if (!direction || !appointmentId)
        return;

    let checkupId = null;

    if (direction === 'SELF') {
        checkupId = null;
    } else if (direction.startsWith('CHECKUP_')) {
        checkupId = Number(
            direction.replace('CHECKUP_', '')
        );

        if (!checkups.some(
            item => item.checkupId === checkupId
        )) {
            return;
        }
    } else {
        return;
    }

    const appointment = doctorAppointments.find(
        item => item.appointmentId === appointmentId
    );

    if (!appointment)
        return;

    const confirmed = confirm(
        `Записаться на приём ${formatDateTime(appointment.appointedTime)}?`
    );

    if (!confirmed)
        return;

    $('bookingSubmit').disabled = true;

    const result = await api(() =>
        protocol.appointmentAdd(
            appointmentId,
            checkupId
        )
    );

    if (result === null) {
        $('bookingSubmit').disabled = false;
        return;
    }

    if (result.success !== true) {
        alert('Не удалось записаться на приём');

        await loadDoctorAppointments();
        return;
    }

    alert('Вы успешно записаны на приём');

    $('appointmentBookForm').reset();

    resetSpecializations();

    await loadAppointments();
    await loadCheckups();
}


$('bookingDirection').addEventListener(
    'change',
    onDirectionChange
);

$('bookingSpecialization').addEventListener(
    'change',
    loadDoctors
);

$('bookingDoctor').addEventListener(
    'change',
    loadDoctorAppointments
);

$('bookingAppointment').addEventListener(
    'change',
    () => {
        $('bookingSubmit').disabled =
            !$('bookingAppointment').value;
    }
);

$('appointmentBookForm').addEventListener(
    'submit',
    bookAppointment
);

async function init() {
    const results = await Promise.all([
        loadAppointments(),
        loadCheckups(),
        loadSpecializations()
    ]);

    if (!results[1] || !results[2])
        return;
}

init();