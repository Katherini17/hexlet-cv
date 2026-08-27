package io.hexlet.cv.audit;

public enum AuditReason {
    USER_NOT_FOUND,
    INVALID_PASSWORD,
    AUTHENTICATION_FAILED,
    EMAIL_TAKEN,
    TOKEN_INVALID,
    TOKEN_MISSING,
    FORBIDDEN,
    CLIENT_ERROR,
    SERVER_ERROR
}
