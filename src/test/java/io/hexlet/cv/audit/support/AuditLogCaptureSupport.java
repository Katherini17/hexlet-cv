package io.hexlet.cv.audit.support;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.hexlet.cv.audit.AuditLogger;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

/**
 * Перехватывает строки журнала аудита на время теста.
 * Аннотаций контекста не несёт: {@code AuditAlertTest} поднимает собственный контекст
 * своими properties, и базовый класс не должен навязывать ему чужой.
 */
public abstract class AuditLogCaptureSupport {

    private Logger auditLogger;
    private ListAppender<ILoggingEvent> appender;
    private Level previousLevel;

    /**
     * Подстроки, которых в журнале быть не должно: локальные части адресов и пароли.
     * Проверяются после каждого теста по всем перехваченным строкам.
     *
     * @return маркеры утечки, пустой список - если проверять нечего
     */
    protected abstract List<String> secretMarkers();

    @BeforeEach
    void attachAuditAppender() {
        appender = new ListAppender<>();
        appender.start();

        auditLogger = (Logger) LoggerFactory.getLogger(AuditLogger.LOGGER_NAME);
        // Фильтр по уровню отрабатывает раньше appender'а, а секции logging
        // в application-test.yml нет: без явной установки тест зависит от прод-настроек
        previousLevel = auditLogger.getLevel();
        auditLogger.setLevel(Level.INFO);
        auditLogger.addAppender(appender);

        appender.list.clear();
    }

    @AfterEach
    void detachAuditAppender() {
        var lines = capturedLines();

        auditLogger.detachAppender(appender);
        auditLogger.setLevel(previousLevel);
        appender.stop();

        var markers = secretMarkers();
        assertThat(lines).allSatisfy(line -> assertThat(line).doesNotContain(markers));
    }

    protected List<ILoggingEvent> capturedEvents() {
        return List.copyOf(appender.list);
    }

    protected List<String> capturedLines() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    /**
     * Разбирает строку журнала по именованным группам шаблона.
     * Имена берутся из самого шаблона, а не из отдельного списка: иначе переименование
     * группы ломало бы тест в рантайме вместо компиляции.
     *
     * @param event   перехваченное событие
     * @param pattern шаблон строки с именованными группами
     * @return значения полей в порядке групп шаблона
     */
    protected Map<String, String> parseFields(ILoggingEvent event, Pattern pattern) {
        var line = event.getFormattedMessage();
        var matcher = pattern.matcher(line);

        assertThat(matcher.matches())
                .as("строка журнала должна совпадать с ожидаемым форматом, получено: %s", line)
                .isTrue();

        var fields = new LinkedHashMap<String, String>();
        matcher.namedGroups().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .forEach(group -> fields.put(group.getKey(), matcher.group(group.getKey())));
        return fields;
    }
}
