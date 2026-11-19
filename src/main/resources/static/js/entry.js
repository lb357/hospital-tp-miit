import { protocol, ApiError } from './protocol.js';



function $(id) {
    return document.getElementById(id);
}

function showStatus(message) {
    $('entryStatus').textContent = message;
}



const params = new URLSearchParams(window.location.search);
const appointmentId = Number(params.get('appointmentId'));

let entryExists = false;
let saving = false;



async function loadEntry() {
    if (!appointmentId || !Number.isInteger(appointmentId)) {
        showStatus('Некорректный идентификатор приёма');
        $('saveButton').disabled = true;
        return;
    }

    try {
        const data = await protocol.entryGet(appointmentId);

        if (!data) {
            throw new Error('Пустой ответ сервера');
        }

        entryExists = true;

        $('entryContent').value = data.content ?? '';

    } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
            entryExists = false;

            $('entryContent').value = '';

            return;
        }

        let message = 'Ошибка связи с сервером';

        if (error instanceof ApiError) {
            message = {
                400: 'Некорректный запрос',
                401: 'Не авторизован',
                403: 'Недостаточно прав',
                409: 'Конфликт данных',
                500: 'Внутренняя ошибка сервера'
            }[error.status] ?? `Ошибка сервера: ${error.status}`;
        }

        alert(message);
        console.error(error);

        $('saveButton').disabled = true;
    }
}


async function saveEntry(event) {
    event.preventDefault();

    if (saving || !appointmentId)
        return;

    const content = $('entryContent').value.trim();

    if (!content) {
        alert('Введите содержание записи');
        return;
    }

    saving = true;
    $('saveButton').disabled = true;

    try {
        let result;

        if (entryExists) {
            result = await protocol.entryUpdate(
                appointmentId,
                content
            );
        } else {
            result = await protocol.entryCreate(
                appointmentId,
                content
            );
        }

        if (!result) {
            throw new Error('Сервер вернул пустой ответ');
        }

        entryExists = true;

        $('entryContent').value = result.content ?? content;

        alert('Запись сохранена');
        window.location.href = '/appointments';
    } catch (error) {
        let message = 'Ошибка связи с сервером';

        if (error instanceof ApiError) {
            message = {
                400: 'Некорректный запрос',
                401: 'Не авторизован',
                403: 'Недостаточно прав',
                404: 'Приём или запись не найдены',
                409: 'Конфликт данных'
            }[error.status] ?? `Ошибка сервера: ${error.status}`;
        }

        alert(message);

        console.error(error);
    } finally {
        saving = false;
        $('saveButton').disabled = false;
    }
}

$('entryForm').addEventListener(
    'submit',
    saveEntry
);


loadEntry();