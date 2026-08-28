package io.hexlet.cv.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.hexlet.cv.model.RefreshToken;
import io.hexlet.cv.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenCleanupJobTest {

    @Autowired
    private RefreshTokenCleanupJob cleanupJob;

    @Autowired
    private RefreshTokenRepository repository;

    @Test
    void purgeExpiredRemovesOnlyExpiredRows() {
        repository.deleteAll();

        var expired = RefreshToken.builder()
                .jti(UUID.randomUUID())
                .userId(1L)
                .familyId(UUID.randomUUID())
                .issuedAt(Instant.now().minusSeconds(3600))
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        repository.save(expired);

        var active = RefreshToken.builder()
                .jti(UUID.randomUUID())
                .userId(1L)
                .familyId(UUID.randomUUID())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        repository.save(active);

        cleanupJob.purgeExpired();

        assertThat(repository.existsById(expired.getJti())).isFalse();
        assertThat(repository.existsById(active.getJti())).isTrue();
    }
}
