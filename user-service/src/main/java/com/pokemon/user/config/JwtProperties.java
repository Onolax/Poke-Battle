package com.pokemon.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app.jwt")
public class JwtProperties {
    private String secret;
    private int expirationHours = 24;
}
