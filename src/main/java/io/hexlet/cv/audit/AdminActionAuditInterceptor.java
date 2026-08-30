package io.hexlet.cv.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Пишет в журнал действия администратора.
 * Работает интерцептором, а не вызовами в контроллерах: исход события определяется статусом
 * ответа, а он известен только после обработки. Читающие запросы пропускаются - журнал
 * фиксирует изменения, а не просмотры.
 */
@Component
@AllArgsConstructor
public class AdminActionAuditInterceptor implements HandlerInterceptor {

    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final AuditLogger auditLogger;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (READ_ONLY_METHODS.contains(request.getMethod())) {
            return;
        }

        var subject = AuditSubject.current();
        var status = HttpStatusCode.valueOf(response.getStatus());

        if (!status.isError()) {
            auditLogger.logSuccess(AuditEventType.ADMIN_ACTION, subject, request);
            return;
        }

        var reason = status.is5xxServerError() ? AuditReason.SERVER_ERROR : AuditReason.CLIENT_ERROR;
        auditLogger.logFailure(AuditEventType.ADMIN_ACTION, subject, reason, request);
    }
}
