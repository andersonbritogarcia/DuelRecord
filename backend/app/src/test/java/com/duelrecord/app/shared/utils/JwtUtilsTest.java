package com.duelrecord.app.shared.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtUtilsTest {

    @Test
    void shouldReturnNullWhenJwtIsNull() {
        assertNull(JwtUtils.extractUserInfo(null));
    }

    @Test
    void shouldExtractClaimsWhenNameIsPresent() {
        Jwt jwt = createJwt("sub-123", Map.of(
                "email", "john@example.com",
                "name", "John Doe",
                "given_name", "John"
        ));

        var userInfo = JwtUtils.extractUserInfo(jwt);

        assertEquals("sub-123", userInfo.sub());
        assertEquals("john@example.com", userInfo.email());
        assertEquals("John Doe", userInfo.name());
    }

    @Test
    void shouldFallbackToGivenNameWhenNameIsBlank() {
        Jwt jwt = createJwt("sub-456", Map.of(
                "email", "jane@example.com",
                "name", "   ",
                "given_name", "Jane"
        ));

        var userInfo = JwtUtils.extractUserInfo(jwt);

        assertEquals("sub-456", userInfo.sub());
        assertEquals("jane@example.com", userInfo.email());
        assertEquals("Jane", userInfo.name());
    }

    @Test
    void shouldFallbackToGivenNameWhenNameIsNull() {
        Jwt jwt = createJwt("sub-789", Map.of(
                "email", "bob@example.com",
                "given_name", "Bob"
        ));

        var userInfo = JwtUtils.extractUserInfo(jwt);

        assertEquals("sub-789", userInfo.sub());
        assertEquals("bob@example.com", userInfo.email());
        assertEquals("Bob", userInfo.name());
    }

    private Jwt createJwt(String subject, Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(c -> c.putAll(claims))
                .build();
    }
}
