package com.pokemon.battle.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Value("${app.jwt.secret:changeme-32-char-secret-for-dev-only}")
    private String jwtSecret;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null) return false;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                String token = param.substring(6);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    attributes.put("userId", claims.getSubject());
                    attributes.put("username", claims.get("username", String.class));
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {}
}
