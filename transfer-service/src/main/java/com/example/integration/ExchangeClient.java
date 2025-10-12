package com.example.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExchangeClient {

    private final WebClient gatewayWebClient;
    private final OAuth2AuthorizedClientManager clientManager;

    private String ccToken() {
        var req = OAuth2AuthorizeRequest.withClientRegistrationId("transfer")
                .principal("transfer")
                .build();
        var client = clientManager.authorize(req);
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Cannot obtain cc token for transfer");
        }
        return client.getAccessToken().getTokenValue();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> getRates() {
        var token = ccToken();
        return gatewayWebClient.get()
                .uri("/api/exchange/rates")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /** Конвертация через базу RUB */
    public BigDecimal convert(BigDecimal amount, String from, String to, Map<String, Map<String, Object>> rates) {
        if (from.equalsIgnoreCase(to)) return amount;

        BigDecimal rub = amount;
        if (!"RUB".equalsIgnoreCase(from)) {
            var r = rates.get(from);
            if (r == null) throw new IllegalStateException("No rate for " + from);
            var sell = new BigDecimal(r.get("sell").toString());  // RUB за 1 from
            rub = amount.multiply(sell);
        }
        if ("RUB".equalsIgnoreCase(to)) return rub;

        var r2 = rates.get(to);
        if (r2 == null) throw new IllegalStateException("No rate for " + to);
        var buy = new BigDecimal(r2.get("buy").toString()); // RUB за 1 to
        return rub.divide(buy, 2, java.math.RoundingMode.HALF_UP);
    }
}
