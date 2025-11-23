package com.example.config;

import com.example.metrics.LoginMetricsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final LoginMetricsFilter loginMetricsFilter;
    private final org.springframework.core.convert.converter.Converter<
            org.springframework.security.oauth2.jwt.Jwt,
            java.util.Collection<org.springframework.security.core.GrantedAuthority>> resourceRolesConverter;

    public SecurityConfig(
            LoginMetricsFilter loginMetricsFilter, org.springframework.core.convert.converter.Converter<
                    org.springframework.security.oauth2.jwt.Jwt,
                    java.util.Collection<org.springframework.security.core.GrantedAuthority>> resourceRolesConverter) {
        this.loginMetricsFilter = loginMetricsFilter;
        this.resourceRolesConverter = resourceRolesConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(resourceRolesConverter);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Actuator: permitAll ---
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/actuator/health/liveness").permitAll()
                        .requestMatchers("/actuator/health/readiness").permitAll()

                        // --- API ---
                        .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                        .requestMatchers("/api/public/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/internal/**").hasAnyRole("cash-writer", "transfer-writer")
                        .requestMatchers("/api/internal/account-info/**").hasAnyRole("transfer-writer", "cash-writer")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
        http.addFilterAfter(loginMetricsFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}