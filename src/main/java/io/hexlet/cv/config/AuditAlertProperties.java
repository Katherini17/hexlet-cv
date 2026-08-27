package io.hexlet.cv.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Пороги, по которым журнал считает серию неудачных входов аномалией.
 * Значения по умолчанию заданы здесь, а не только в application.yml: детектор должен
 * работать и в конфигурации, где секции app.audit.alerts нет вовсе.
 */
@Component
@ConfigurationProperties(prefix = "app.audit.alerts")
@Getter
@Setter
public class AuditAlertProperties {

    private boolean enabled = true;

    private Duration window = Duration.ofMinutes(5);

    private int failureThreshold = 10;

    private int maxTrackedKeys = 10000;
}
