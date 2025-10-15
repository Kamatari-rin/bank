package com.example.dto.internal;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ApplyCashRequest(
        @NotNull UUID userKeycloakId,
        @NotNull BigDecimal delta,             // >0 пополнение, <0 снятие
        String idempotencyKey                  // опционально — на будущее
) {}