package com.example.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final WebClient gatewayWebClient;
    private final OAuth2AuthorizedClientManager clientManager;

    private String ccToken() {
        var req = OAuth2AuthorizeRequest.withClientRegistrationId("transfer")
                .principal("transfer")
                .build();
        var client = clientManager.authorize(req);
        if (client == null || client.getAccessToken() == null)
            throw new IllegalStateException("Cannot obtain cc token for transfer");
        return client.getAccessToken().getTokenValue();
    }

    public void notify(UUID userId, String type, String title, String message) {
        var token = ccToken();
        try {
            gatewayWebClient.post()
                    .uri("/api/notifications/internal/notify")
                    .headers(h -> h.setBearerAuth(token))
                    .bodyValue(new Body(type, userId, title, message))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("notify sent: type={} user={} title='{}'", type, userId, title);
        } catch (Exception e) {
            log.warn("notify failed: {}", e.toString());
        }
    }

    private record Body(String type, UUID userId, String title, String message) {}
}
