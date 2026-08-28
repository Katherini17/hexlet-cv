package io.hexlet.cv.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.cv.audit.support.AuditLogCaptureSupport;
import io.hexlet.cv.dto.user.auth.LoginRequestDTO;
import io.hexlet.cv.dto.user.auth.RegistrationRequestDTO;
import io.hexlet.cv.handler.GlobalExceptionHandler;
import io.hexlet.cv.model.User;
import io.hexlet.cv.model.enums.RoleType;
import io.hexlet.cv.repository.UserRepository;
import io.hexlet.cv.security.TokenService;
import io.hexlet.cv.util.JWTUtils;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

/**
 * Проверяет журнал security-событий: состав полей, формат строки, уровень записи
 * и отсутствие в логе пароля и локальной части email.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLoggingTest extends AuditLogCaptureSupport {

    private static final String USER_EMAIL = "audit-subject@gmail.com";
    private static final String USER_LOCAL_PART = "audit-subject";
    private static final String MASKED_USER_EMAIL = "au***@gmail.com";
    private static final String PASSWORD = "pwd-marker-9f3a";

    private static final String NEW_USER_EMAIL = "newcomer-audit@gmail.com";
    private static final String NEW_USER_LOCAL_PART = "newcomer-audit";
    private static final String MASKED_NEW_USER_EMAIL = "ne***@gmail.com";

    private static final String ADMIN_EMAIL = "admin-marker@example.com";
    private static final String ADMIN_LOCAL_PART = "admin-marker";
    private static final String MASKED_ADMIN_EMAIL = "ad***@example.com";

    private static final String PLACEHOLDER = "-";

    private static final Pattern AUDIT_LINE = Pattern.compile(
            "^\\[AUDIT\\] event=(?<event>\\S+) subject=(?<subject>\\S+) outcome=(?<outcome>\\S+) "
                    + "reason=(?<reason>\\S+) ip=(?<ip>\\S+) method=(?<method>\\S+) path=(?<path>\\S+)$");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private JWTUtils jwtUtils;

    @MockitoSpyBean
    private TokenService tokenService;

    @Override
    protected List<String> secretMarkers() {
        return List.of(USER_LOCAL_PART, NEW_USER_LOCAL_PART, ADMIN_LOCAL_PART, PASSWORD);
    }

    @BeforeEach
    void createUser() {
        userRepository.deleteAll();

        var user = new User();
        user.setEmail(USER_EMAIL);
        user.setEncryptedPassword(encoder.encode(PASSWORD));
        user.setFirstName("Audit");
        user.setLastName("Subject");
        user.setRole(RoleType.CANDIDATE);
        userRepository.save(user);
    }

    @AfterEach
    void removeUsers() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLogSuccessfulLoginWithMaskedSubject() throws Exception {
        mockMvc.perform(post("/users/sign_in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginDto(USER_EMAIL, PASSWORD))))
                .andExpect(status().isFound());

        var event = singleAuditEvent();

        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(auditFields(event))
                .containsEntry("event", AuditEventType.LOGIN.name())
                .containsEntry("subject", MASKED_USER_EMAIL)
                .containsEntry("outcome", AuditOutcome.SUCCESS.name())
                .containsEntry("reason", PLACEHOLDER)
                .containsEntry("ip", "127.0.0.1")
                .containsEntry("method", "POST")
                .containsEntry("path", "/users/sign_in");
    }

    @Test
    void shouldLogSuccessfulRegistration() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(registrationDto(NEW_USER_EMAIL))))
                .andExpect(status().isFound());

        var event = singleAuditEvent();

        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(auditFields(event))
                .containsEntry("event", AuditEventType.REGISTRATION.name())
                .containsEntry("subject", MASKED_NEW_USER_EMAIL)
                .containsEntry("outcome", AuditOutcome.SUCCESS.name())
                .containsEntry("path", "/users");
    }

    @Test
    void shouldLogSuccessfulLogout() throws Exception {
        mockMvc.perform(post("/users/sign_out")
                        .with(candidateJwt())
                        .cookie(new Cookie("refresh_token", jwtUtils.generateRefreshToken(USER_EMAIL))))
                .andExpect(status().isFound());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.LOGOUT.name())
                .containsEntry("subject", MASKED_USER_EMAIL)
                .containsEntry("outcome", AuditOutcome.SUCCESS.name())
                .containsEntry("reason", PLACEHOLDER);
    }

    @Test
    void shouldLogLogoutWithoutRefreshTokenAsFailure() throws Exception {
        mockMvc.perform(post("/users/sign_out").with(candidateJwt()))
                .andExpect(status().isFound());

        assertThat(auditFields(singleAuditEvent()))
                .as("выход без refresh-токена отзывать нечего - для журнала это аномалия")
                .containsEntry("event", AuditEventType.LOGOUT.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.TOKEN_MISSING.name());
    }

    @Test
    void shouldLogSuccessfulTokenRefresh() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", jwtUtils.generateRefreshToken(USER_EMAIL))))
                .andExpect(status().isNoContent());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.TOKEN_REFRESH.name())
                .containsEntry("outcome", AuditOutcome.SUCCESS.name())
                .containsEntry("subject", MASKED_USER_EMAIL);
    }

    @Test
    void shouldLogTokenRefreshFailureWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("refresh_token", "not-a-jwt")))
                .andExpect(status().isUnauthorized());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.TOKEN_REFRESH.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.TOKEN_INVALID.name());
    }

    @Test
    void shouldLogFailedLoginWhenPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/users/sign_in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginDto(USER_EMAIL, "another-password"))))
                .andExpect(status().isUnauthorized());

        var event = singleAuditEvent();

        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(auditFields(event))
                .containsEntry("event", AuditEventType.LOGIN.name())
                .containsEntry("subject", MASKED_USER_EMAIL)
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.INVALID_PASSWORD.name());
    }

    @Test
    void shouldLogFailedLoginWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/users/sign_in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginDto("no-such-user@gmail.com", PASSWORD))))
                .andExpect(status().isUnauthorized());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.LOGIN.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.USER_NOT_FOUND.name());
    }

    @Test
    void shouldLogUnauthorizedWhenProtectedPageRequestedWithoutCookie() throws Exception {
        mockMvc.perform(get("/account/newsletters/edit"))
                .andExpect(status().isUnauthorized());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.UNAUTHORIZED.name())
                .containsEntry("subject", PLACEHOLDER)
                .containsEntry("outcome", AuditOutcome.FAILURE.name());
    }

    @Test
    void shouldLogUnauthorizedWithTokenInvalidWhenCookieIsBroken() throws Exception {
        mockMvc.perform(get("/account/newsletters/edit").cookie(new Cookie("access_token", "not-a-jwt")))
                .andExpect(status().isUnauthorized());

        assertThat(auditFields(singleAuditEvent()))
                .as("предъявленный и отвергнутый токен должен отличаться в журнале от отсутствующего")
                .containsEntry("event", AuditEventType.UNAUTHORIZED.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.TOKEN_INVALID.name());
    }

    @Test
    void shouldLogUnhandledErrorWithErrorLevel() {
        var request = new MockHttpServletRequest("GET", "/api/unstable/endpoint");
        request.setRemoteAddr("127.0.0.1");

        globalExceptionHandler.handleAll(new IllegalStateException("IT happens"), request,
                new RedirectAttributesModelMap());

        var event = singleAuditEvent();

        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(auditFields(event))
                .containsEntry("event", AuditEventType.UNHANDLED_ERROR.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.SERVER_ERROR.name())
                .containsEntry("path", "/api/unstable/endpoint");
    }

    @Test
    void shouldLogAccessDeniedWhenNonAdminEntersAdminArea() throws Exception {
        mockMvc.perform(get("/admin").with(candidateJwt()).header("X-Inertia", "true"))
                .andExpect(status().isForbidden());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.ACCESS_DENIED.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.FORBIDDEN.name());
    }

    @Test
    void shouldLogTokenRefreshFailureWhenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.TOKEN_REFRESH.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.TOKEN_MISSING.name());
    }

    @Test
    void shouldLogRegistrationFailureWhenEmailIsTaken() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(registrationDto(USER_EMAIL))))
                .andExpect(status().isConflict());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.REGISTRATION.name())
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.EMAIL_TAKEN.name());
    }

    @Test
    void shouldLogRegistrationFailureWhenTokenIssuingFails() throws Exception {
        doThrow(new BadCredentialsException("Токены не выданы"))
                .when(tokenService).authenticateAndGenerate(any(), any());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(registrationDto(NEW_USER_EMAIL))))
                .andExpect(status().isInternalServerError());

        var event = singleAuditEvent();

        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(auditFields(event))
                .as("запрос завершился ошибкой - записи об успешной регистрации быть не должно")
                .containsEntry("event", AuditEventType.REGISTRATION.name())
                .containsEntry("subject", MASKED_NEW_USER_EMAIL)
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.AUTHENTICATION_FAILED.name());
    }

    @Test
    void shouldLogAdminActionWithClientErrorWhenSectionIsUnknown() throws Exception {
        mockMvc.perform(delete("/admin/marketing/unknown-section/1").with(adminJwt()))
                .andExpect(status().isNotFound());

        assertThat(auditFields(singleAuditEvent()))
                .containsEntry("event", AuditEventType.ADMIN_ACTION.name())
                .containsEntry("subject", MASKED_ADMIN_EMAIL)
                .containsEntry("outcome", AuditOutcome.FAILURE.name())
                .containsEntry("reason", AuditReason.CLIENT_ERROR.name())
                .containsEntry("method", "DELETE");
    }

    @Test
    void shouldNotLogReadOnlyAdminRequests() throws Exception {
        mockMvc.perform(get("/admin").with(adminJwt()).header("X-Inertia", "true"))
                .andExpect(status().isOk());

        assertThat(capturedEvents()).isEmpty();
    }

    private ILoggingEvent singleAuditEvent() {
        var events = capturedEvents();
        assertThat(events)
                .as("на один HTTP-запрос должна приходиться ровно одна аудит-строка")
                .hasSize(1);
        return events.getFirst();
    }

    private Map<String, String> auditFields(ILoggingEvent event) {
        return parseFields(event, AUDIT_LINE);
    }

    private LoginRequestDTO loginDto(String email, String password) {
        var dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private RegistrationRequestDTO registrationDto(String email) {
        var dto = new RegistrationRequestDTO();
        dto.setEmail(email);
        dto.setPassword(PASSWORD);
        dto.setFirstName("Audit");
        dto.setLastName("Subject");
        dto.setTerms(true);
        return dto;
    }

    private RequestPostProcessor adminJwt() {
        return jwt().jwt(builder -> builder.subject(ADMIN_EMAIL))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private RequestPostProcessor candidateJwt() {
        return jwt().jwt(builder -> builder.subject(USER_EMAIL))
                .authorities(new SimpleGrantedAuthority("ROLE_CANDIDATE"));
    }
}
