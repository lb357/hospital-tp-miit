class ApiError extends Error {
    constructor(status, data = null) {
        super(`HTTP error ${status}`);
        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }
}

async function post(endpoint, body) {
    const options = {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (body !== undefined)
        options.body = JSON.stringify(body);

    const response = await fetch(endpoint, options);

    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok)
        throw new ApiError(response.status, data);

    return data;
}

export { ApiError };

export const protocol = {
    // AUTH

    login: (userId, password) =>
        post('/api/auth/login', { userId, password }),

    logout: () =>
        post('/api/auth/logout'),

    signup: (lastName, firstName, patronymicName, password,
             role, patientGender, doctorSpecializationId) =>
        post('/api/auth/signup', {
            lastName,
            firstName,
            patronymicName,
            password,
            role,
            patientGender,
            doctorSpecializationId
        }),

    // USER

    userUpdate: (userId, lastName, firstName, patronymicName) =>
        post('/api/user/update', {
            userId,
            lastName,
            firstName,
            patronymicName
        }),

    userProfile: () =>
        post('/api/user/profile'),

    userGet: () =>
        post('/api/user/get'),

    userDelete: (userId) =>
        post('/api/user/delete', { userId }),

    specializationCreate: (name) =>
        post('/api/user/specialization/create', { name }),

    specializationUpdate: (specializationId) =>
        post('/api/user/specialization/update', { specializationId }),

    specializationGet: () =>
        post('/api/user/specialization/get'),

    specialists: (specializationId) =>
        post('/api/user/specialists', { specializationId }),

    // CHECKUP

    checkupGet: (patientId) =>
        post('/api/checkup/get', { patientId }),

    checkupCreate: (patientId, specializationIds) =>
        post('/api/checkup/create', {
            patientId,
            specializationIds
        }),

    checkupDelete: (checkupId) =>
        post('/api/checkup/delete', { checkupId }),

    // APPOINTMENT

    appointmentGet: () =>
        post('/api/appointment/get'),

    appointmentCreate: (doctorId, room, appointedTime) =>
        post('/api/appointment/create', {
            doctorId,
            room,
            appointedTime
        }),

    appointmentDelete: (appointmentId) =>
        post('/api/appointment/delete', { appointmentId }),

    appointmentAdd: (appointmentId, checkupId) =>
        post('/api/appointment/add', {
            appointmentId,
            checkupId
        }),

    entryCreate: (appointmentId, content) =>
        post('/api/appointment/entry/create', {
            appointmentId,
            content
        }),

    entryGet: (appointmentId) =>
        post('/api/appointment/entry/get', { appointmentId }),

    entryUpdate: (appointmentId, content) =>
        post('/api/appointment/entry/update', {
            appointmentId,
            content
        }),

    entryRecord: (patientId) =>
        post('/api/appointment/entry/record', { patientId })
};



export async function request(apiCallback, errorCallback) {
    try {
        return await apiCallback();
    } catch (error) {
        let message;
        if (error instanceof ApiError) {
            message = {
                400: 'Некорректный запрос',
                401: 'Не авторизован',
                403: 'Недостаточно прав',
                404: 'Ресурс не найден',
                409: 'Конфликт данных'
            }[error.status];
        } else {
            message = 'Ошибка связи с сервером';
        }
        errorCallback(message);
        return null;
    }
}
