package io.hexlet.cv.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.cv.audit.support.AuditLogCaptureSupport;
import io.hexlet.cv.config.AuditAlertProperties;
import io.hexlet.cv.dto.user.auth.LoginRequestDTO;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Проверяет алерты на всплеск неудачных входов.
 * Пороги задаются здесь, а не берутся из application-test.yml: в нём алерты выключены,
 * иначе счётчик, общий для всех тестов контекста, добавлял бы строки в чужие проверки.
 * Собственные properties поднимают отдельный контекст - счётчик изолирован от остальных тестов.
 */
@SpringBootTest(properties = {
    "app.audit.alerts.enabled=true",
    "app.audit.alerts.failure-threshold=3",
    "app.audit.alerts.window=1m"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditAlertTest extends AuditLogCaptureSupport {

    // Маркеры, которых нет в остальном журнале: на общем слове ассерт doesNotContain
    // ничего бы не доказывал
    private static final String EMAIL_LOCAL_PART = "alert-probe";
    private static final String EMAIL_DOMAIN = "@example.com";
    private static final String MASKED_EMAIL_PREFIX = "al***";
    private static final String PASSWORD = "alert-pwd-7c1b";

    private static final String ALERT_PREFIX = "[AUDIT-ALERT]";

    // Порядок полей - часть контракта: по нему строится оповещение
    private static final Pattern ALERT_LINE = Pattern.compile(
            "^\\[AUDIT-ALERT\\] alert=(?<alert>\\S+) scope=(?<scope>\\S+) key=(?<key>\\S+) "
                    + "count=(?<count>\\d+) window=(?<window>\\d+)s$");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    // Пороги читаются из тех же properties, что и приложение: иначе тест мог бы
    // проверять число, с которым детектор не работает
    @Autowired
    private AuditAlertProperties properties;

    @Override
    protected List<String> secretMarkers() {
        return List.of(EMAIL_LOCAL_PART, PASSWORD);
    }

    @Test
    void shouldNotAlertBelowThreshold() throws Exception {
        var ip = "10.0.0.1";
        for (int attempt = 1; attempt < threshold(); attempt++) {
            failedLogin(EMAIL_LOCAL_PART + "-below-" + attempt + EMAIL_DOMAIN, ip);
        }

        assertThat(alertEvents()).isEmpty();
    }

    @Test
    void shouldAlertOnSpikeFromSingleAddress() throws Exception {
        var ip = "10.0.0.2";
        // Адреса разные: сработать должен счётчик узла, а не счётчик аккаунта
        for (int attempt = 1; attempt <= threshold(); attempt++) {
            failedLogin(EMAIL_LOCAL_PART + "-addr-" + attempt + EMAIL_DOMAIN, ip);
        }

        var alert = singleAlert();

        assertThat(alert.getLevel()).isEqualTo(Level.ERROR);
        assertThat(parseFields(alert, ALERT_LINE))
                .containsEntry("alert", AuditAlertType.LOGIN_FAILURE_SPIKE.name())
                .containsEntry("scope", AuditAlertScope.IP.name())
                .containsEntry("key", ip)
                .containsEntry("count", String.valueOf(threshold()))
                .containsEntry("window", String.valueOf(properties.getWindow().toSeconds()));
    }

    @Test
    void shouldAlertOnSpikeAgainstSingleAccount() throws Exception {
        var email = EMAIL_LOCAL_PART + "-account" + EMAIL_DOMAIN;
        // Узлы разные: подбор пароля к одному аккаунту с ботнета виден только по субъекту
        for (int attempt = 1; attempt <= threshold(); attempt++) {
            failedLogin(email, "10.0.1." + attempt);
        }

        assertThat(parseFields(singleAlert(), ALERT_LINE))
                .containsEntry("scope", AuditAlertScope.SUBJECT.name())
                .containsEntry("key", MASKED_EMAIL_PREFIX + EMAIL_DOMAIN)
                .containsEntry("count", String.valueOf(threshold()));
    }

    @Test
    void shouldNotRepeatAlertUntilNextSeriesIsCollected() throws Exception {
        var ip = "10.0.0.3";
        for (int attempt = 1; attempt <= threshold(); attempt++) {
            failedLogin(EMAIL_LOCAL_PART + "-repeat-" + attempt + EMAIL_DOMAIN, ip);
        }
        assertThat(alertEvents()).hasSize(1);

        failedLogin(EMAIL_LOCAL_PART + "-repeat-next" + EMAIL_DOMAIN, ip);

        assertThat(alertEvents())
                .as("после срабатывания счёт начинается заново, иначе перебор зальёт журнал")
                .hasSize(1);
    }

    private int threshold() {
        return properties.getFailureThreshold();
    }

    private void failedLogin(String email, String ip) throws Exception {
        mockMvc.perform(post("/users/sign_in")
                        .with(from(ip))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginDto(email))))
                .andExpect(status().isUnauthorized());
    }

    private List<ILoggingEvent> alertEvents() {
        return capturedEvents().stream()
                .filter(event -> event.getFormattedMessage().startsWith(ALERT_PREFIX))
                .toList();
    }

    private ILoggingEvent singleAlert() {
        var alerts = alertEvents();
        assertThat(alerts)
                .as("серия должна давать ровно один алерт, по той размерности, где набран порог")
                .hasSize(1);
        return alerts.getFirst();
    }

    private LoginRequestDTO loginDto(String email) {
        var dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPassword(PASSWORD);
        return dto;
    }

    private RequestPostProcessor from(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }
}
