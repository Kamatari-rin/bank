package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/actuator/**").permitAll()
                        // публичное чтение уведомлений под user JWT
                        .requestMatchers(HttpMethod.GET,"/api/public/mine").authenticated()
                        // внутренний вызов — только role notifications-writer
                        .requestMatchers(HttpMethod.POST,"/api/internal/notify").hasRole("notifications-writer")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(o->o.jwt(j->j
                        .jwkSetUri("http://keycloak:8080/realms/bank/protocol/openid-connect/certs")
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return conv;
    }

    /** читаем resource_access.notifications-service.roles -> ROLE_* */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object ra = jwt.getClaim("resource_access");
        if (!(ra instanceof Map<?, ?> raMap)) return List.of();

        Object clientObj = raMap.get("notifications-service");
        if (!(clientObj instanceof Map<?, ?> clientMap)) return List.of();

        Object rolesObj = clientMap.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) return List.of();

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
