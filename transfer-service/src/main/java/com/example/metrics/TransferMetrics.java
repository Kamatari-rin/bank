package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TransferMetrics {

    private final MeterRegistry registry;

    public TransferMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void transferFailure(String type, String sender, String fromAcc, String toAcc, String reason) {
        Counter.builder("transfer.failure.total")
                .description("Failed money transfers")
                .tag("type", type)              // own / external
                .tag("sender", sender)
                .tag("from", fromAcc)
                .tag("to", toAcc)
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void transferBlocked(String type, String sender, String fromAcc, String toAcc, String reason) {
        Counter.builder("transfer.blocked.total")
                .description("Blocked by anti-fraud")
                .tag("type", type)
                .tag("sender", sender)
                .tag("from", fromAcc)
                .tag("to", toAcc)
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void transferSuccess(String type, String sender, String fromAcc, String toAcc) {
        Counter.builder("transfer.success.total")
                .description("Successful completed transfers")
                .tag("type", type)
                .tag("sender", sender)
                .tag("from", fromAcc)
                .tag("to", toAcc)
                .register(registry)
                .increment();
    }
}
