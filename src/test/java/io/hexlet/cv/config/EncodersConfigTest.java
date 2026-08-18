package io.hexlet.cv.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hexlet.cv.model.User;
import io.hexlet.cv.model.enums.RoleType;
import io.hexlet.cv.repository.UserRepository;
import io.hexlet.cv.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EncodersConfigTest {

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    @Qualifier("refreshTokenDecoder")
    private JwtDecoder refreshTokenDecoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        var user = new User();
        user.setEmail("test@gmail.com");
        user.setEncryptedPassword(new BCryptPasswordEncoder().encode("test_password"));
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setRole(RoleType.CANDIDATE);
        userRepository.save(user);
    }

    @Test
    void accessDecoderRejectsRefreshToken() {
        String refreshToken = jwtUtils.generateRefreshToken("test@gmail.com");
        assertThrows(JwtException.class, () -> jwtDecoder.decode(refreshToken));
    }

    @Test
    void refreshDecoderRejectsAccessToken() {
        String accessToken = jwtUtils.generateAccessToken("test@gmail.com");
        assertThrows(JwtException.class, () -> refreshTokenDecoder.decode(accessToken));
    }

    @Test
    void refreshDecoderAcceptsMatchingRefreshToken() {
        String refreshToken = jwtUtils.generateRefreshToken("test@gmail.com");
        assertDoesNotThrow(() -> refreshTokenDecoder.decode(refreshToken));
    }
}
