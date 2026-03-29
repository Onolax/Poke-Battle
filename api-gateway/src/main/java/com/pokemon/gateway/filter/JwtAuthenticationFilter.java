package com.pokemon.gateway.filter;

import com.pokemon.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey key;

    public JwtAuthenticationFilter(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (path.equals("/auth") || path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (path.startsWith("/ws/")) {
            // WebSocket/SockJS: browser cannot send Authorization header, token arrives as query param
            token = exchange.getRequest().getQueryParams().getFirst("token");
            if (token == null) return unauthorized(exchange);
        } else {
            return unauthorized(exchange);
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .headers(h -> h.remove("X-User-Id"))
                    .headers(h -> h.remove("X-Username"))
                    .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
