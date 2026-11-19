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


async function loadProfile() {
    const container = $('profileContainer');

    const data = await api(() =>
        protocol.userProfile()
    );

    container.replaceChildren();

    if (data === null) {
        container.textContent = 'Не удалось загрузить профиль';
        return;
    }

    const user = data.user ?? data;

    const roles = {
        patient: 'Пациент',
        doctor: 'Врач',
        admin: 'Администратор'
    };

    const name = fullName(user);

    const role = roles[data.role?.toLowerCase()]
        ?? data.role
        ?? 'Не указана';

    const nameElement = document.createElement('p');

    nameElement.textContent =
        `ФИО: ${name || 'Не указано'}`;

    const roleElement = document.createElement('p');

    roleElement.textContent =
        `Роль: ${role}`;

    container.append(
        nameElement,
        roleElement
    );
}


loadProfile();