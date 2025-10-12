package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurity(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(exchange -> {
                    var cfg = new CorsConfiguration();
                    var env = exchange.getApplicationContext().getEnvironment();
                    cfg.setAllowedOrigins(List.of(env.getProperty("cors.allowed-origins", "http://localhost:3000")));
                    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    cfg.setAllowCredentials(true);
                    return cfg;
                }))
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**").permitAll()
                        // регистрация доступна без токена через gateway
                        .pathMatchers(HttpMethod.POST, "/api/accounts/register").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }

    /**
     * Единственный глобальный фильтр: добавляет X-User-* из JWT.
     * Если запрос анонимный (регистрация) — пропускаем без заголовков.
     */
    @Bean
    public GlobalFilter userContextHeadersFilter() {
        return (exchange, chain) ->
                exchange.getPrincipal()
                        .ofType(JwtAuthenticationToken.class)
                        .flatMap(auth -> {
                            var jwt = auth.getToken();
                            var uid = String.valueOf(jwt.getClaims().getOrDefault("sub", auth.getName()));
                            var uname = String.valueOf(
                                    jwt.getClaims().getOrDefault("preferred_username",
                                            jwt.getClaims().getOrDefault("email", auth.getName()))
                            );
                            var mutated = exchange.getRequest().mutate()
                                    .header("X-User-Id", uid)
                                    .header("X-User-Name", uname)
                                    .build();
                            return chain.filter(exchange.mutate().request(mutated).build());
                        })
                        .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }
}
