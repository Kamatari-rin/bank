package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CashRequest(
        @NotNull UUID accountId,
        @NotNull @Positive BigDecimal amount,
        String idempotencyKey
) {}