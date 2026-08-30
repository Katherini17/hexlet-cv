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
    PASSWORD_CHANGE,
    PASSWORD_RESET_REQUEST
}
