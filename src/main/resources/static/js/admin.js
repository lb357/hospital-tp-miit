
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



let users = [];
let specializations = [];


function getRole(user) {
    return (user.role ?? user.userRole ?? 'UNKNOWN').toUpperCase();
}

function isPatient(user) {
    return getRole(user) === 'PATIENT'
        || user.gender !== undefined
        || user.patient?.gender !== undefined;
}

function isDoctor(user) {
    return getRole(user) === 'DOCTOR'
        || user.specialization !== undefined;
}

function userDescription(user) {
    const role = getRole(user);
    let details = '';

    if (isPatient(user)) {
        const patient = user.patient ?? user;

        details = `Пол: ${
            patient.gender ? 'Мужской' : 'Женский'
        }`;
    } else if (isDoctor(user)) {
        const specialization = user.specialization;

        details = specialization
            ? `Специальность: ${specialization.specializationId} - ${specialization.specializationName}`
            : '';
    }

    const displayRole = {
        PATIENT: 'Пациент',
        DOCTOR: 'Врач',
        ADMIN: 'Администратор'
    }[role] ?? 'Неизвестно';

    return `[${user.userId} - ${displayRole}] ${fullName(user)} / ${details}`;
}

async function loadUsers() {
    const data = await api(() => protocol.userGet());

    if (data === null)
        return;

    users = toArray(data, 'users', 'data', 'items')
        .map(item => ({
            ...item.user,
            role: item.role,
            gender: item.gender,
            specialization: item.specialization
        }));

    showList($('usersList'), users, userDescription);

    fillSelect(
        $('userSelect'),
        users,
        user => user.userId,
        user => `${user.userId} - ${fullName(user)}`
    );

    $('updateUserId').value = '';
    $('userRecordButton').disabled = true;
}

$('userSelect').addEventListener('change', () => {
    const user = users.find(
        user => String(user.userId) === $('userSelect').value
    );

    if (!user) {
        $('updateUserId').value = '';
        $('userRecordButton').disabled = true;
        return;
    }

    $('userRecordButton').disabled = !isPatient(user);

    $('updateUserId').value = user.userId;
    $('updateLastName').value = user.lastName ?? '';
    $('updateFirstName').value = user.firstName ?? '';
    $('updatePatronymicName').value = user.patronymicName ?? '';
});

$('userDeleteButton').addEventListener('click', async () => {
    const userId = Number($('userSelect').value);

    if (!userId)
        return;

    if (!confirm('Удалить пользователя?'))
        return;

    const result = await api(() =>
        protocol.userDelete(userId)
    );

    if (result === null)
        return;

    await loadUsers();
});

$('userForm').addEventListener('submit', async event => {
    event.preventDefault();

    const userId = Number($('updateUserId').value);

    if (!userId)
        return;

    const result = await api(() =>
        protocol.userUpdate(
            userId,
            $('updateLastName').value,
            $('updateFirstName').value,
            $('updatePatronymicName').value
        )
    );

    if (result === null)
        return;

    alert('Пользователь изменён');
    await loadUsers();
});

$('userRecordButton').addEventListener('click', () => {
    const userId = Number($('userSelect').value);

    if (!userId || $('userRecordButton').disabled)
        return;

    window.location.href = `/record?patientId=${userId}`;
});


$('newRole').addEventListener('change', () => {
    const role = $('newRole').value;

    $('patientFields').hidden = role !== 'PATIENT';
    $('doctorFields').hidden = role !== 'DOCTOR';
});

function downloadCredentials(lastName, firstName, userId, password) {
    const content =
        `Приветствуем в больничной информационной системе, ${lastName} ${firstName}!\n` +
        `Ниже представлены ваши данные для входа в учетную запись.\n\n` +
        `Идентификационный номер: ${userId}\n` +
        `Пароль: ${password}\n`;

    const blob = new Blob(
        [content],
        { type: 'text/plain;charset=utf-8' }
    );

    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `Данные-пользователя-${userId}.txt`;

    document.body.append(link);
    link.click();
    link.remove();

    URL.revokeObjectURL(url);
}

$('userCreateForm').addEventListener('submit', async event => {
    event.preventDefault();

    const role = $('newRole').value;
    const password = $('newPassword').value;

    const result = await api(() =>
        protocol.signup(
            $('newLastName').value,
            $('newFirstName').value,
            $('newPatronymicName').value,
            password,
            role,
            role === 'PATIENT'
                ? $('newGender').value === 'true'
                : null,
            role === 'DOCTOR'
                ? Number($('newDoctorSpecialization').value)
                : null
        )
    );

    if (result === null)
        return;

    const user = result.user;

    downloadCredentials(
        user.lastName,
        user.firstName,
        user.userId,
        password
    );

    alert('Пользователь добавлен');

    $('userCreateForm').reset();

    $('patientFields').hidden = false;
    $('doctorFields').hidden = true;

    await loadUsers();
});


async function loadSpecializations() {
    const data = await api(() =>
        protocol.specializationGet()
    );

    if (data === null)
        return;

    specializations = toArray(data, 'specializations', 'data');

    showList(
        $('specializationsList'),
        specializations,
        item => `${item.specializationId} - ${item.specializationName}`
    );

    fillSelect(
        $('specializationSelect'),
        specializations,
        item => item.specializationId,
        item => `${item.specializationId} - ${item.specializationName}`
    );

    fillSelect(
        $('newDoctorSpecialization'),
        specializations,
        item => item.specializationId,
        item => item.specializationName
    );
}

$('specializationCreateForm').addEventListener('submit', async event => {
    event.preventDefault();

    const result = await api(() =>
        protocol.specializationCreate(
            $('specializationName').value
        )
    );

    if (result === null)
        return;

    alert('Специальность добавлена');
    $('specializationCreateForm').reset();

    await loadSpecializations();
});

$('specializationDeleteForm').addEventListener('submit', async event => {
    event.preventDefault();

    const specializationId = Number(
        $('specializationSelect').value
    );

    if (!specializationId)
        return;

    if (!confirm('Удалить специальность?'))
        return;

    const result = await api(() =>
        protocol.specializationDelete(specializationId)
    );

    if (result === null)
        return;

    await loadSpecializations();
});


async function init() {
    await loadSpecializations();
    await loadUsers();
}

init();
