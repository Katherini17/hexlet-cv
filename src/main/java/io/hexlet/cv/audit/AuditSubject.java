package io.hexlet.cv.audit;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Достаёт субъекта события из контекста аутентификации.
 * Нужен там, где событие возникает вне обработчика с телом запроса: выход, отказ в доступе,
 * действие администратора. Для входа и регистрации субъект берётся из DTO - там он известен
 * до того, как аутентификация состоялась.
 */
public final class AuditSubject {

    private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    private AuditSubject() {
    }

    /**
     * Возвращает идентификатор текущего пользователя.
     * Анонимный запрос даёт null, а не "anonymousUser": подстановкой плейсхолдера
     * занимается AuditLogger, и вымышленное имя в журнале выглядело бы как реальный субъект.
     *
     * @return email текущего пользователя либо null, если запрос анонимный
     */
    public static String current() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return TRUST_RESOLVER.isAuthenticated(authentication) ? authentication.getName() : null;
    }
}
