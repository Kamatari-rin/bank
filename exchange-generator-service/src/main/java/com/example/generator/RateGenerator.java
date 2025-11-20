package com.example.generator;

import com.example.dto.exchange.ExchangeRateMessage;
import com.example.dto.exchange.RateRowMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RateGenerator {

    private final KafkaTemplate<String, ExchangeRateMessage> kafka;

    @Value("${app.kafka.exchange-topic}")
    private String topic;

    @Scheduled(fixedDelay = 1000)
    public void tick() {

        var msg = new ExchangeRateMessage(
                "RUB",
                Map.of(
                        "USD", new RateRowMessage("USD", jiggle(89.5, 0.2), jiggle(90.5, 0.2)),
                        "CNY", new RateRowMessage("CNY", jiggle(11.8, 0.05), jiggle(12.2, 0.05))
                ),
                Instant.now()
        );

        kafka.send(topic, "exchange-rates", msg)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to send exchange rates: {}", ex.toString());
                    } else {
                        log.info("Exchange rates sent: currencies={}", msg.rates().keySet());
                    }
                });
    }

    private static RateRowMessage row(String currency, double buy, double sell, double spread) {
        return new RateRowMessage(currency, jiggle(buy, spread), jiggle(sell, spread));
    }

    private static BigDecimal jiggle(double base, double spread) {
        if (spread <= 0.0) return bd(base);
        double delta = ThreadLocalRandom.current().nextDouble(-spread, spread);
        return bd(Math.max(0.0001, base + delta));
    }

    private static BigDecimal bd(double v) {
        return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP);
    }
}
