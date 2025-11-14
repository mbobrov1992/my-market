package ru.yandex.practicum.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        String[] permitAntPatterns = new String[]{
                "/v3/api-docs/**",
                "/swagger-ui/**"
        };

        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(permitAntPatterns).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(customizer -> customizer
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }
}
