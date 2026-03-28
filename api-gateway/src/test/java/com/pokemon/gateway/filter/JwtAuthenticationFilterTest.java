package com.pokemon.gateway.filter;

import com.pokemon.gateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ServerWebExchange;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    static final String SECRET = "changeme-32-char-secret-for-dev-only";
    static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    JwtAuthenticationFilter filter;
    GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        filter = new JwtAuthenticationFilter(props);

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void authPath_skipsFilter() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/login").build());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void validToken_injectsHeadersAndForwards() {
        UUID userId = UUID.randomUUID();
        String token = buildToken(userId, "ash", 1);
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/teams")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(captor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ServerWebExchange captured = captor.getValue();
        assertThat(captured.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo(userId.toString());
        assertThat(captured.getRequest().getHeaders().getFirst("X-Username")).isEqualTo("ash");
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void missingHeader_returns401() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/teams").build());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void malformedToken_returns401() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/teams")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer garbage.token.value")
                        .build());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void wrongKeyToken_returns401() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "wrong-key-different-from-main-secret-key!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user-id")
                .claim("username", "ash")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(wrongKey)
                .compact();

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/teams")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // Helper
    private String buildToken(UUID userId, String username, int expiryHours) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryHours * 3600_000L))
                .signWith(KEY)
                .compact();
    }
}
