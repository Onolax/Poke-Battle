package com.pokemon.user.integration;

import com.pokemon.user.dto.LoginRequest;
import com.pokemon.user.dto.LoginResponse;
import com.pokemon.user.dto.RegisterRequest;
import com.pokemon.user.dto.RegisterResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate http;

    @Test
    void registerThenLogin_returnsValidJwt() {
        // Register
        RegisterRequest reg = new RegisterRequest("ash", "ash@pallet.com", "pikachu123");
        ResponseEntity<RegisterResponse> regResp = http.postForEntity(
                "/auth/register", reg, RegisterResponse.class);

        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(regResp.getBody()).isNotNull();
        assertThat(regResp.getBody().username()).isEqualTo("ash");
        assertThat(regResp.getBody().userId()).isNotNull();

        // Login
        LoginRequest login = new LoginRequest("ash", "pikachu123");
        ResponseEntity<LoginResponse> loginResp = http.postForEntity(
                "/auth/login", login, LoginResponse.class);

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        String token = loginResp.getBody().token();
        assertThat(token).isNotBlank();

        // Parse token and verify claims
        SecretKey key = Keys.hmacShaKeyFor(
                "changeme-32-char-secret-for-dev-only".getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        assertThat(claims.getSubject()).isEqualTo(regResp.getBody().userId().toString());
        assertThat(claims.get("username", String.class)).isEqualTo("ash");
    }

    @Test
    void loginWithBadPassword_returns401() {
        // Register first
        http.postForEntity("/auth/register",
                new RegisterRequest("brock", "brock@pewter.com", "onix"), RegisterResponse.class);

        // Wrong password
        ResponseEntity<Void> resp = http.postForEntity(
                "/auth/login", new LoginRequest("brock", "wrongpass"), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
