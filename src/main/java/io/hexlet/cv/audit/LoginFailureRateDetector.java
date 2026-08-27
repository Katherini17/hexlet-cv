package io.hexlet.cv.audit;

import io.hexlet.cv.config.AuditAlertProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Замечает всплеск неудачных входов и просит журнал записать алерт.
 * Считает в памяти процесса: при нескольких экземплярах приложения каждый видит только
 * свою долю запросов, поэтому порог стоит понимать как "не меньше, чем", а не точное число.
 */
@Component
@RequiredArgsConstructor
public class LoginFailureRateDetector {

    private final AuditAlertProperties properties;
    private final AuditLogger auditLogger;

    /**
     * Счётчики по ключу "размерность + значение". Значение субъекта хранится сырым:
     * маскированные адреса схлопнулись бы в один ключ и один счётчик на весь домен.
     * Маскирует уже AuditLogger, на выводе.
     */
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Учитывает одну неудачную попытку входа сразу в обеих размерностях.
     *
     * @param subject адрес, под которым пытались войти, может быть null
     * @param request текущий запрос, может быть null при вызове вне request-scope
     */
    public void recordFailure(String subject, HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return;
        }
        // Часы читаются один раз на попытку: обе размерности считают одно и то же событие
        var now = Instant.now();
        register(AuditAlertScope.IP, request == null ? null : request.getRemoteAddr(), now);
        register(AuditAlertScope.SUBJECT, subject, now);
    }

    /**
     * Прибавляет попытку к окну одного ключа и пишет алерт, когда счёт дошёл до порога.
     *
     * @param scope размерность, по которой ведётся счёт
     * @param value значение ключа, может быть null или небезопасным для записи
     * @param now   момент попытки
     */
    private void register(AuditAlertScope scope, String value, Instant now) {
        if (value == null || value.isBlank() || !LogFieldSanitizer.isSafe(value)) {
            return;
        }

        var key = scope.name() + ":" + value;
        var window = properties.getWindow();

        // Порядок проверок важен: пока место есть, до поиска ключа дело не доходит
        if (!hasRoomForNewKey(now, window) && !windows.containsKey(key)) {
            return;
        }

        // Ключ удаляется, а не обнуляется: следующий алерт по нему возможен только после
        // ещё одной полной серии - иначе перебор зальёт журнал. Удаление и служит признаком
        // срабатывания, поэтому счёт наружу выносить нечем - он равен порогу
        var updated = windows.compute(key, (ignored, current) -> {
            var next = current == null || isExpired(current, now, window)
                    ? new Window(now, 1)
                    : new Window(current.startedAt(), current.count() + 1);
            return next.count() >= properties.getFailureThreshold() ? null : next;
        });

        if (updated == null) {
            auditLogger.logAlert(AuditAlertType.LOGIN_FAILURE_SPIKE, scope, value,
                    properties.getFailureThreshold(), window);
        }
    }

    /**
     * Решает, можно ли завести ещё один ключ, освобождая место за счёт истёкших окон.
     * Отказ означает пропущенный алерт, и это сознательный размен: исчерпать память
     * на записях, заведённых самим перебором, хуже, чем не заметить часть всплеска.
     *
     * @param now    текущий момент
     * @param window длина окна наблюдения
     * @return true, если место под новый ключ есть
     */
    private boolean hasRoomForNewKey(Instant now, Duration window) {
        if (windows.size() < properties.getMaxTrackedKeys()) {
            return true;
        }
        windows.values().removeIf(tracked -> isExpired(tracked, now, window));
        return windows.size() < properties.getMaxTrackedKeys();
    }

    private static boolean isExpired(Window tracked, Instant now, Duration window) {
        return !now.isBefore(tracked.startedAt().plus(window));
    }

    /**
     * Окно наблюдения по одному ключу. Неизменяемое: обновление идёт заменой значения
     * внутри compute, поэтому счётчик не портится при одновременных запросах.
     *
     * @param startedAt момент начала окна
     * @param count     число неудач, попавших в окно
     */
    private record Window(Instant startedAt, int count) {
    }
}
