package com.example.integration;

import com.example.dto.internal.ApplyCashRequest;
import com.example.dto.internal.ApplyCashResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountsClient {

    private final WebClient accountsWebClient;
    private final OAuth2AuthorizedClientManager clientManager;

    private String getServiceToken() {
        var authReq = OAuth2AuthorizeRequest.withClientRegistrationId("cash-service")
                .principal("cash-service") // фиктивный principal ID для cc
                .build();
        var client = clientManager.authorize(authReq);
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Unable to obtain client_credentials token for cash-service");
        }
        return client.getAccessToken().getTokenValue();
    }

    public ApplyCashResponse applyCash(UUID accountId, ApplyCashRequest req) {
        String token = getServiceToken();
        return accountsWebClient.post()
                .uri("/api/accounts/internal/accounts/{id}/apply-cash", accountId)
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ApplyCashResponse.class)
                .block();
    }
}
