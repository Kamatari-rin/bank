package com.example.integration;

import com.example.dto.internal.AccountInfoInternal;
import com.example.dto.internal.TransferInternalRequest;
import com.example.dto.internal.TransferInternalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountsClient {

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

    public AccountInfoInternal accountInfo(UUID accountId) {
        String token = ccToken();
        return gatewayWebClient.get()
                .uri("/api/accounts/internal/account-info/{id}", accountId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(AccountInfoInternal.class)
                .block();
    }

    public TransferInternalResponse transfer(TransferInternalRequest r) {
        String token = ccToken();
        return gatewayWebClient.post()
                .uri("/api/accounts/internal/transfer")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(r)
                .retrieve()
                .bodyToMono(TransferInternalResponse.class)
                .block();
    }
}
