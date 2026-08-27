package io.hexlet.cv.config;

import io.hexlet.cv.audit.AdminActionAuditInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Включает аудит административных действий.
 * Пути берутся из AdminPaths - того же списка, которым SecurityConfig закрывает зону ролью
 * администратора: журнал должен покрывать ровно то, что защищено.
 */
@Configuration
@AllArgsConstructor
public class AuditWebConfig implements WebMvcConfigurer {

    private final AdminActionAuditInterceptor adminActionAuditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminActionAuditInterceptor)
                .addPathPatterns(AdminPaths.adminZonePatterns());
    }
}
