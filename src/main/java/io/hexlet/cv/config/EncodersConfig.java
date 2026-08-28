package io.hexlet.cv.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.hexlet.cv.repository.UserRepository;
import io.hexlet.cv.util.JWTUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@RequiredArgsConstructor
public class EncodersConfig {

    private static final String INVALID_TOKEN = "invalid_token";

    private final RsaKeyProperties rsaKeys;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeys.getPublicKey()).privateKey(rsaKeys.getPrivateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKeys.getPublicKey()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), mustNotBeRefresh(),
                issuerValid(), audienceValid(), accessSessionValid()
        ));
        return decoder;
    }

    @Bean
    JwtDecoder refreshTokenDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKeys.getPublicKey()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), mustBeRefresh(), tokenVersionValid(),
                issuerValid(), audienceValid(), refreshClaimsPresent()
        ));
        return decoder;
    }

    /**
     * Access-токен: tokenVersion (глобальный kill switch) и живость сессии — одним запросом.
     * Заменяет tokenVersionValid() на access-декодере.
     */
    private OAuth2TokenValidator<Jwt> accessSessionValid() {
        return jwt -> {
            Long tokenVersion = jwt.getClaim(JWTUtils.CLAIM_TOKEN_VERSION);
            if (tokenVersion == null) {
                return invalid("Missing tokenVersion claim");
            }
            String rawFamilyId = jwt.getClaimAsString(JWTUtils.CLAIM_FAMILY_ID);
            if (rawFamilyId == null) {
                // Фаза 1: токен выпущен до релиза — проверяем только tokenVersion.
                return jwtProperties.isEnforceSessionClaims()
                        ? invalid("Missing familyId claim")
                        : tokenVersionValid().validate(jwt);
            }
            UUID familyId;
            try {
                familyId = UUID.fromString(rawFamilyId);
            } catch (IllegalArgumentException e) {
                return invalid("Malformed familyId claim");
            }
            return userRepository.isSessionValid(jwt.getSubject(), tokenVersion, familyId)
                    ? OAuth2TokenValidatorResult.success()
                    : invalid("Session revoked");
        };
    }

    /**
     * Refresh-токен обязан нести jti и familyId. Обязательный валидатор, не опциональный:
     * без него токены старого формата дойдут до TokenService и дадут 500 вместо 401.
     */
    private OAuth2TokenValidator<Jwt> refreshClaimsPresent() {
        return jwt -> {
            boolean present = jwt.getId() != null
                    && jwt.getClaimAsString(JWTUtils.CLAIM_FAMILY_ID) != null;
            if (present || !jwtProperties.isEnforceSessionClaims()) {
                return OAuth2TokenValidatorResult.success();
            }
            return invalid("Missing jti or familyId");
        };
    }

    private OAuth2TokenValidatorResult invalid(String message) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(INVALID_TOKEN, message, null));
    }

    private OAuth2TokenValidator<Jwt> mustNotBeRefresh() {
        return jwt -> "refresh".equals(jwt.getClaimAsString("type"))
                ? OAuth2TokenValidatorResult.failure(
                new OAuth2Error(INVALID_TOKEN, "Refresh token cannot authenticate requests", null))
                : OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidator<Jwt> mustBeRefresh() {
        return jwt -> "refresh".equals(jwt.getClaimAsString("type"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                new OAuth2Error(INVALID_TOKEN, "Expected a refresh token", null));
    }

    private OAuth2TokenValidator<Jwt> tokenVersionValid() {
        return jwt -> {
            Long claimVersion = jwt.getClaim("tokenVersion");
            if (claimVersion == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(INVALID_TOKEN, "Missing tokenVersion claim", null));
            }
            return userRepository.findByEmail(jwt.getSubject())
                    .filter(user -> user.getTokenVersion() == claimVersion)
                    .map(user -> OAuth2TokenValidatorResult.success())
                    .orElseGet(() -> OAuth2TokenValidatorResult.failure(
                            new OAuth2Error(INVALID_TOKEN, "Token revoked", null)));
        };
    }

    private OAuth2TokenValidator<Jwt> issuerValid() {
        return new JwtIssuerValidator(jwtProperties.getIssuer());
    }

    private OAuth2TokenValidator<Jwt> audienceValid() {
        return jwt -> jwt.getAudience().contains(jwtProperties.getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(INVALID_TOKEN, "Wrong audience", null));
    }
}
