package com.example.integration;

import com.example.dto.internal.BlockerDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerClient {

    private final WebClient gatewayWebClient;
    private final OAuth2AuthorizedClientManager clientManager;

    private String ccToken() {
        var req = OAuth2AuthorizeRequest.withClientRegistrationId("cash-service")
                .principal("cash-service")
                .build();
        var client = clientManager.authorize(req);
        if (client == null || client.getAccessToken() == null)
            throw new IllegalStateException("Cannot obtain cc token for cash-service");
        return client.getAccessToken().getTokenValue();
    }

    public CheckResponse check(CheckRequest req) {
        try {
            var token = ccToken();
            return gatewayWebClient.post()
                    .uri("/api/blocker/internal/check")
                    .headers(h -> h.setBearerAuth(token))
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(CheckResponse.class)
                    .doOnNext(r -> log.info("blocker: allowed={} score={} reason={}", r.allowed(), r.score(), r.reason()))
                    .block();
        } catch (Exception e) {
            log.warn("blocker call failed: {}", e.toString());
            return new CheckResponse(true, "fail-open", 0);
        }
    }
}

