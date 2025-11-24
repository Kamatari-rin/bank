package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ExchangeGeneratorMetrics {

    private final AtomicLong lastPushEpochSeconds;
    private final Counter successCounter;
    private final Counter failureCounter;

    public ExchangeGeneratorMetrics(MeterRegistry registry) {
        this.lastPushEpochSeconds =
                registry.gauge("exchange_generator_last_push_epoch_seconds", new AtomicLong(0));

        this.successCounter = Counter.builder("exchange_generator_push_success_total")
                .description("Successful FX rate pushes")
                .register(registry);

        this.failureCounter = Counter.builder("exchange_generator_push_failure_total")
                .description("Failed FX rate pushes")
                .register(registry);
    }

    public void markSuccess() {
        lastPushEpochSeconds.set(Instant.now().getEpochSecond());
        successCounter.increment();
    }

    public void markFailure() {
        failureCounter.increment();
    }
}
