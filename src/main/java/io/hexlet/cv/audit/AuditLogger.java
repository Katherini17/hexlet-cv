package io.hexlet.cv.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

/**
 * Пишет журнал security-событий: одна строка фиксированного формата на одно событие.
 * На один HTTP-запрос приходится не больше одной записи - см. {@link #LOGGED_MARKER}.
 */
@Component
public class AuditLogger {

    /**
     * Имя категории журнала. Совпадает с именем пакета, но задано явно: на него ссылаются
     * настройка уровня в application.yml и тест, поэтому адрес категории не должен
     * меняться от переезда класса в другой пакет.
     */
    public static final String LOGGER_NAME = "io.hexlet.cv.audit";

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger(LOGGER_NAME);

    /**
     * Порядок полей - часть контракта: по нему строятся grep и алерты. Ни одно значение
     * не должно содержать пробел, иначе разбор строки съедет на соседнее поле;
     * за это отвечают PiiMasker и LogFieldSanitizer.
     */
    private static final String EVENT_TEMPLATE =
            "[AUDIT] event={} subject={} outcome={} reason={} ip={} method={} path={}";

    /**
     * Шаблон алерта. Поля другие, чем у события, поэтому и префикс другой: по нему
     * оповещение отделяется от рутинных строк журнала одним grep.
     */
    private static final String ALERT_TEMPLATE =
            "[AUDIT-ALERT] alert={} scope={} key={} count={} window={}s";

    private static final String PLACEHOLDER = "-";

    /**
     * Отметка "по этому запросу запись уже сделана". Одно событие проходит сразу через
     * несколько точек (обработчик исключения, интерцептор, handleAll), и без отметки
     * на запрос пришлось бы несколько строк вместо одной.
     */
    private static final String LOGGED_MARKER = AuditLogger.class.getName() + ".logged";

    public void logSuccess(AuditEventType eventType, String subject, HttpServletRequest request) {
        logEvent(AuditOutcome.SUCCESS, eventType, subject, null, request);
    }

    public void logFailure(AuditEventType eventType, String subject, AuditReason reason, HttpServletRequest request) {
        logEvent(AuditOutcome.FAILURE, eventType, subject, reason, request);
    }

    private void logEvent(AuditOutcome outcome, AuditEventType eventType, String subject,
                          AuditReason reason, HttpServletRequest request) {
        try {
            var level = resolveLevel(outcome, reason);
            if (!AUDIT_LOG.isEnabledForLevel(level) || isAlreadyLogged(request)) {
                return;
            }
            AUDIT_LOG.atLevel(level).log(EVENT_TEMPLATE,
                    nullSafe(eventType),
                    maskSubject(subject),
                    nullSafe(outcome),
                    nullSafe(reason),
                    safe(request == null ? null : request.getRemoteAddr()),
                    safe(request == null ? null : request.getMethod()),
                    safe(request == null ? null : request.getRequestURI()));
            markLogged(request);
        } catch (RuntimeException e) {
            AUDIT_LOG.error("[AUDIT] не удалось записать событие {}", eventType, e);
        }
    }

    /**
     * Пишет алерт об аномалии. Уровень всегда ERROR: оповещение обязано отделяться
     * от неудачных попыток, из которых оно собрано, - те идут на WARN.
     *
     * <p>Отметка "по этому запросу запись уже сделана" здесь не проверяется и не ставится:
     * алерт возникает в том же запросе, что и неудачный вход, и с отметкой его вытеснила бы
     * запись самого события.
     *
     * @param alert  вид аномалии
     * @param scope  размерность, по которой сработал порог
     * @param key    значение ключа: адрес или субъект, маскируется здесь же
     * @param count  число событий, набранных в окне
     * @param window длина окна наблюдения
     */
    public void logAlert(AuditAlertType alert, AuditAlertScope scope, String key, int count, Duration window) {
        try {
            AUDIT_LOG.error(ALERT_TEMPLATE,
                    nullSafe(alert),
                    nullSafe(scope),
                    maskKey(scope, key),
                    count,
                    window == null ? PLACEHOLDER : String.valueOf(window.toSeconds()));
        } catch (RuntimeException e) {
            AUDIT_LOG.error("[AUDIT] не удалось записать алерт {}", alert, e);
        }
    }

    /**
     * Выбирает уровень записи. Серверная ошибка поднимается до ERROR: иначе она не отделится
     * от рутинных отказов доступа, по которым алерты не строят.
     *
     * @param outcome исход события
     * @param reason  причина отказа, null для успеха
     * @return уровень, на котором пишется строка
     */
    private static Level resolveLevel(AuditOutcome outcome, AuditReason reason) {
        if (reason == AuditReason.SERVER_ERROR) {
            return Level.ERROR;
        }
        return outcome == AuditOutcome.SUCCESS ? Level.INFO : Level.WARN;
    }

    /**
     * Отсутствие субъекта - случай журнала, а не маскировщика, поэтому плейсхолдер
     * подставляется здесь, а PiiMasker получает только непустое значение.
     *
     * @param subject идентификатор субъекта события, обычно email
     * @return замаскированный субъект либо плейсхолдер
     */
    private static String maskSubject(String subject) {
        return subject == null || subject.isBlank() ? PLACEHOLDER : PiiMasker.maskEmail(subject);
    }

    /**
     * Готовит ключ алерта к записи. Маскирование выбирается по размерности, а не по виду
     * значения: субъект - это адрес и маскируется, адрес узла - нет, иначе алерт
     * перестанет указывать, откуда идёт перебор.
     *
     * @param scope размерность, по которой сработал порог
     * @param key   значение ключа
     * @return значение, пригодное для записи в журнал
     */
    private static String maskKey(AuditAlertScope scope, String key) {
        return scope == AuditAlertScope.SUBJECT ? maskSubject(key) : safe(key);
    }

    /**
     * Пропускает значение в журнал как есть либо заменяет плейсхолдером. Закрывает сразу оба
     * источника пустоты: отсутствующее значение (вызов вне request-scope) и небезопасное.
     *
     * @param value значение поля, может быть null
     * @return значение поля либо плейсхолдер
     */
    private static String safe(String value) {
        return value != null && !value.isBlank() && LogFieldSanitizer.isSafe(value) ? value : PLACEHOLDER;
    }

    private static String nullSafe(Enum<?> value) {
        return value == null ? PLACEHOLDER : value.name();
    }

    private static boolean isAlreadyLogged(HttpServletRequest request) {
        return request != null && request.getAttribute(LOGGED_MARKER) != null;
    }

    private static void markLogged(HttpServletRequest request) {
        if (request != null) {
            request.setAttribute(LOGGED_MARKER, Boolean.TRUE);
        }
    }
}
