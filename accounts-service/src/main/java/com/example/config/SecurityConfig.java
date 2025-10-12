package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final org.springframework.core.convert.converter.Converter<
            org.springframework.security.oauth2.jwt.Jwt,
            java.util.Collection<org.springframework.security.core.GrantedAuthority>> resourceRolesConverter;

    public SecurityConfig(
            org.springframework.core.convert.converter.Converter<
                    org.springframework.security.oauth2.jwt.Jwt,
                    java.util.Collection<org.springframework.security.core.GrantedAuthority>> resourceRolesConverter) {
        this.resourceRolesConverter = resourceRolesConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(resourceRolesConverter);

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/register").permitAll()
                        .requestMatchers("/api/public/**").authenticated()   // <- вот это
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/internal/**").hasAnyRole("cash-writer","transfer-writer")
                        .requestMatchers("/api/internal/account-info/**").hasAnyRole("transfer-writer","cash-writer")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .build();
    }
}