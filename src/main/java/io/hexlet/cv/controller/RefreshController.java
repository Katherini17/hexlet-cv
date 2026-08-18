package io.hexlet.cv.controller;

import io.hexlet.cv.security.TokenCookieService;
import io.hexlet.cv.security.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshController {

    private final TokenService tokenService;
    private final TokenCookieService tokenCookieService;

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken,
                                         HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            var tokens = tokenService.refresh(refreshToken);
            var cookies = tokenCookieService.buildCookies(tokens);
            response.addHeader(HttpHeaders.SET_COOKIE, cookies.access().toString());
            response.addHeader(HttpHeaders.SET_COOKIE, cookies.refresh().toString());
            return ResponseEntity.noContent().build();
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
