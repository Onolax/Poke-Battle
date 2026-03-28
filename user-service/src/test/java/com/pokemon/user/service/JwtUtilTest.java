package com.pokemon.user.service;

import com.pokemon.user.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("changeme-32-char-secret-for-dev-only");
        props.setExpirationHours(24);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void generatedToken_parsesBack() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "ash");

        Claims claims = jwtUtil.validate(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("username", String.class)).isEqualTo("ash");
    }

    @Test
    void expiredToken_throws() {
        // Create a JwtUtil with -1 hour expiry to simulate expired token
        JwtProperties props = new JwtProperties();
        props.setSecret("changeme-32-char-secret-for-dev-only");
        props.setExpirationHours(-1);
        JwtUtil expiredUtil = new JwtUtil(props);

        String token = expiredUtil.generate(UUID.randomUUID(), "ash");

        assertThatThrownBy(() -> jwtUtil.validate(token))
                .isInstanceOf(JwtException.class);
    }
}
