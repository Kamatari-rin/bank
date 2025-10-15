
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

@Configuration
public class SecurityJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwks = "http://keycloak:8080/realms/bank/protocol/openid-connect/certs";

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwks).build();

        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefault();
        decoder.setJwtValidator(validator);

        return decoder;
    }
}
