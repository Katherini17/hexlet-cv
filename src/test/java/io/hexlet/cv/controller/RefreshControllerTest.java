package io.hexlet.cv.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;

import jakarta.servlet.http.Cookie;
import io.hexlet.cv.model.User;
import io.hexlet.cv.model.enums.RoleType;
import io.hexlet.cv.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

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

    private String cookieValue(MvcResult result, String name) {
        return java.util.Arrays.stream(result.getResponse().getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AssertionError("cookie " + name + " not found"));
    }

    private MvcResult loginAs(String email, String password) throws Exception {
        var body = """
            {"email": "%s", "password": "%s"}
            """.formatted(email, password);
        return mockMvc.perform(post("/users/sign_in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Test
    void refreshTokenCannotAuthenticateProtectedEndpoint() throws Exception {
        var login = loginAs("test@gmail.com", "test_password");
        String refreshToken = cookieValue(login, "refresh_token");

        mockMvc.perform(get("/account/knowledge")
                        .cookie(new Cookie("access_token", refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenCannotBeUsedAtRefreshEndpoint() throws Exception {
        var login = loginAs("test@gmail.com", "test_password");
        String accessToken = cookieValue(login, "access_token");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReturnsNewAccessAndRefreshTokens() throws Exception {
        var login = loginAs("test@gmail.com", "test_password");
        String refreshToken = cookieValue(login, "refresh_token");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.containsString("access_token"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.containsString("refresh_token"))));
    }

    @Test
    void expiredOrGarbageRefreshTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "not-a-real-jwt")))
                .andExpect(status().isUnauthorized());
    }
}
