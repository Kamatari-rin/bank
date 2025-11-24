package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ExchangeMetrics {

    private final AtomicLong lastUpdateEpochSeconds;
    private final Counter failures;

    public ExchangeMetrics(MeterRegistry registry) {
        this.lastUpdateEpochSeconds =
                registry.gauge("exchange_rates_last_update_epoch_seconds", new AtomicLong(0));

        this.failures = Counter.builder("exchange_rates_update_failures_total")
                .description("Failed FX rate updates from Kafka")
                .register(registry);
    }

    public void markSuccess() {
        lastUpdateEpochSeconds.set(Instant.now().getEpochSecond());
    }

    public void markFailure() {
        failures.increment();
    }
}
