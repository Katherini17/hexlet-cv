package io.hexlet.cv.audit;

public enum AuditEventType {
    LOGIN,
    LOGOUT,
    REGISTRATION,
    TOKEN_REFRESH,
    ACCESS_DENIED,
    UNAUTHORIZED,
    ADMIN_ACTION,
    UNHANDLED_ERROR,
    // Producer'а пока нет: смена и сброс пароля в продукте не реализованы. Значения
    // заведены заранее, чтобы словарь событий не менялся вместе с этими сценариями
    PASSWORD_CHANGE,
    PASSWORD_RESET_REQUEST
}
