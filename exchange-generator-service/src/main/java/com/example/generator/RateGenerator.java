package com.example.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RateGenerator {

    private final OAuth2AuthorizedClientManager clientManager;
    private final WebClient gateway;

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        String token = clientToken();

        var body = Map.of(
                "base", "RUB",
                "rates", Map.of(
                        "USD", row("USD", 89.5, 90.5, 0.2),
                        "CNY", row("CNY", 11.8, 12.2, 0.05)
                )
        );

        gateway.post()
                .uri("/api/exchange/internal/rates")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v -> log.info("Rates pushed (base=RUB): {}", ((Map<?,?>)body.get("rates")).keySet()))
                .doOnError(err -> log.warn("Failed to push rates: {}", err.toString()))
                .block();
    }

    private String clientToken() {
        var req = OAuth2AuthorizeRequest.withClientRegistrationId("generator")
                .principal("generator").build();
        var c = clientManager.authorize(req);
        if (c == null || c.getAccessToken() == null) {
            throw new IllegalStateException("No cc token");
        }
        return c.getAccessToken().getTokenValue();
    }

    private Map<String, Object> row(String cur, double buy, double sell, double spread) {
        double b = jiggle(buy, spread);
        double s = jiggle(sell, spread);
        return Map.of(
                "currency", cur,
                "buy", bd(b),
                "sell", bd(s)
        );
    }

    private static double jiggle(double base, double spread) {
        if (spread <= 0.0) return base;               // ключевой фикс
        double delta = ThreadLocalRandom.current().nextDouble(-spread, spread);
        return Math.max(0.0001, base + delta);
    }

    private static BigDecimal bd(double v) {
        return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP);
    }
}
