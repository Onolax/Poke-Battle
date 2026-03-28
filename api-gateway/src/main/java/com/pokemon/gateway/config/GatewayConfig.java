package com.pokemon.gateway.config;

import com.pokemon.gateway.filter.JwtAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class GatewayConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtProperties jwtProperties) {
        return new JwtAuthenticationFilter(jwtProperties);
    }
}
