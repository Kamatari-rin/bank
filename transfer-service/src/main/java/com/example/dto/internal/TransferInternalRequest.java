package com.example.dto.internal;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferInternalRequest(
        @NotNull UUID senderKeycloakId,
        @NotNull UUID fromAccountId,
        @NotNull UUID toAccountId,
        @NotNull BigDecimal amountFrom,
        @NotNull BigDecimal amountTo
) {}
