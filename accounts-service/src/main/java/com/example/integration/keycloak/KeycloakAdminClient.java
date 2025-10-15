package com.example.integration.keycloak;

import com.example.config.KeycloakAdminProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminClient {

    private final WebClient web;
    private final KeycloakAdminProperties props;

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

    private String getAdminToken() {
        return web.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", props.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", props.clientId())
                        .with("client_secret", props.clientSecret()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::accessToken)
                .block();
    }

    public UUID createUser(String username, String firstName, String lastName, String email, boolean enabled) {
        var token = getAdminToken();
        var resp = web.post()
                .uri("/admin/realms/{realm}/users", props.realm())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", username,
                        "firstName", firstName,
                        "lastName", lastName,
                        "email", email,
                        "enabled", enabled
                ))
                .retrieve()
                .toBodilessEntity()
                .block();

        var location = resp != null ? resp.getHeaders().getLocation() : null;
        if (location == null) {
            throw new IllegalStateException("No Location header from Keycloak when creating user");
        }
        var id = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
        var kcId = UUID.fromString(id);
        log.debug("KC createUser: username={} email={} kcId={}", username, email, kcId);
        return kcId;
    }

    public void setPassword(UUID userId, String password) {
        var token = getAdminToken();
        web.put()
                .uri("/admin/realms/{realm}/users/{id}/reset-password", props.realm(), userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("type", "password", "value", password, "temporary", false))
                .retrieve()
                .toBodilessEntity()
                .block();
        log.debug("KC setPassword: kcId={}", userId);
    }
}
