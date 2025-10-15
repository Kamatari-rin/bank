package com.example.dto;

import com.example.entity.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BankAccountDto(
        UUID id,
        UUID userId,
        Currency currency,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {}
