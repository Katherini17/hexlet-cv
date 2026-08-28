package io.hexlet.cv.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hexlet.cv.model.User;
import io.hexlet.cv.model.enums.RoleType;
import io.hexlet.cv.repository.UserRepository;
import io.hexlet.cv.support.TokenTestHelper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.jwt.enforce-session-claims=true")
class LegacyRefreshTokenEnforcedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private TokenTestHelper tokenHelper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        var user = new User();
        user.setEmail("test@gmail.com");
        user.setEncryptedPassword(encoder.encode("test_password"));
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setRole(RoleType.CANDIDATE);
        userRepository.save(user);
    }

    // 7.2 (обязательный): токен старого формата при enforce=true → 401, а не 500.
    @Test
    void legacyRefreshTokenIsRejectedWhenEnforced() throws Exception {
        String legacyToken = tokenHelper.legacyRefreshToken("test@gmail.com", 0);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", legacyToken)))
                .andExpect(status().isUnauthorized());
    }
}
