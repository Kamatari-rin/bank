package com.example.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationsClient {
    private final WebClient gatewayWebClient;
    private final OAuth2AuthorizedClientManager clientManager;

    private String ccToken() {
        var req = OAuth2AuthorizeRequest.withClientRegistrationId("cash-service")
                .principal("cash-service")
                .build();
        var client = clientManager.authorize(req);
        if (client == null || client.getAccessToken() == null)
            throw new IllegalStateException("Cannot obtain cc token for notifications");
        return client.getAccessToken().getTokenValue();
    }

    public void notify(UUID userId, String title, String message) {
        String token = ccToken();
        gatewayWebClient.post()
                .uri("/api/notifications/internal/notify")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(Map.of(
                        "userId", userId,
                        "title", title,
                        "message", message
                ))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}

