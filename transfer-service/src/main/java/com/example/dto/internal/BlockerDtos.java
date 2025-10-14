package com.example.dto.internal;


import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class BlockerDtos {

    public enum OperationType { DEPOSIT, WITHDRAW, TRANSFER_OWN, TRANSFER_EXTERNAL }

    public record CheckRequest(
            @NotNull OperationType operation,
            @NotNull UUID userKeycloakId,
            UUID sourceAccountId,
            UUID targetAccountId,
            @NotNull BigDecimal amount,
            @NotNull Instant timestampUtc
    ) {}

    public record CheckResponse(
            boolean allowed,
            String reason,
            int score
    ) {
        public static CheckResponse ok() { return new CheckResponse(true, "ok", 0); }
        public static CheckResponse deny(String reason, int score) { return new CheckResponse(false, reason, score); }
    }
}
