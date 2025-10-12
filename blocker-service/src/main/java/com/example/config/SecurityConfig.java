package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String CLIENT_ID = "blocker-service";

    private static Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Object ra = jwt.getClaims().get("resource_access");
        if (!(ra instanceof Map<?, ?> map)) return List.of();
        Object client = map.get(CLIENT_ID);
        if (!(client instanceof Map<?, ?> cm)) return List.of();
        Object roles = cm.get("roles");
        if (!(roles instanceof Collection<?> rr)) return List.of();
        return rr.stream()
                .map(Object::toString)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }

    private static AbstractAuthenticationToken toAuth(Jwt jwt) {
        var auths = new ArrayList<GrantedAuthority>(extractClientRoles(jwt));
        // На крайний случай: разрешим конкретным azp (необязательно)
        var azp = jwt.getClaimAsString("azp");
        if ("cash-service".equals(azp) || "transfer-service".equals(azp)) {
            auths.add(new SimpleGrantedAuthority("ROLE_blocker-check"));
        }
        return new JwtAuthenticationToken(jwt, auths, jwt.getSubject());
    }

    @Bean
    public SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/internal/check").hasRole("blocker-check")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(j -> j.jwtAuthenticationConverter(SecurityConfig::toAuth)))
                .build();
    }
}
