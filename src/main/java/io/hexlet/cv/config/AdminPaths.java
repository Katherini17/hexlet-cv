package io.hexlet.cv.config;

import java.util.List;

/**
 * Шаблоны путей административной зоны - единственное место, где она описана.
 * Зона нужна двум независимым механизмам: SecurityConfig закрывает её ролью администратора,
 * AuditWebConfig вешает на неё аудит действий.
 */
public final class AdminPaths {

    private static final List<String> ADMIN_ZONE = List.of(
            "/admin/**",
            "/*/admin/**",
            "/*/admin/",
            "/api/pages/sections",
            "/api/pages/sections/**"
    );

    private AdminPaths() {
    }

    public static String[] adminZonePatterns() {
        return ADMIN_ZONE.toArray(String[]::new);
    }
}
