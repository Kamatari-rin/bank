package com.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String CLIENT_ID = "exchange-service"; // имя клиента в Keycloak

    /** Достаём client roles из resource_access[CLIENT_ID].roles */
    private static Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Object ra = jwt.getClaims().get("resource_access");
        if (!(ra instanceof Map<?, ?> map)) return List.of();
        Object clientObj = map.get(CLIENT_ID);
        if (!(clientObj instanceof Map<?, ?> cMap)) return List.of();
        Object rolesObj = cMap.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) return List.of();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Конвертер Jwt → Authentication с нашими ролями и fallback по azp */
    private static AbstractAuthenticationToken toAuth(Jwt jwt) {
        var auths = new ArrayList<GrantedAuthority>();
        auths.addAll(extractClientRoles(jwt));

        // Fallback: если azp == exchange-generator, даём право как exchange-writer
        String azp = jwt.getClaimAsString("azp");
        if ("exchange-generator".equals(azp)) {
            auths.add(new SimpleGrantedAuthority("ROLE_exchange-writer"));
        }
        return new JwtAuthenticationToken(jwt, auths, jwt.getSubject());
    }

    @Bean
    public SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/internal/rates").hasRole("exchange-writer")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(j -> j.jwtAuthenticationConverter(SecurityConfig::toAuth)))
                // Diagnostic filter — покажет, какие роли реально увидели
                .addFilterAfter(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                            throws ServletException, IOException {
                        var ctx = org.springframework.security.core.context.SecurityContextHolder.getContext();
                        var auth = ctx.getAuthentication();
                        if (auth instanceof JwtAuthenticationToken jwtAuth) {
                            var roles = jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
                            var azp = jwtAuth.getToken().getClaimAsString("azp");
                            var aud = jwtAuth.getToken().getAudience();
                            if (request.getRequestURI().startsWith("/api/internal/rates")) {
                                log.info("auth on /api/internal/rates: azp={}, aud={}, roles={}", azp, aud, roles);
                            }
                        }
                        chain.doFilter(request, response);
                    }
                }, org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class)
                .build();
    }
}
