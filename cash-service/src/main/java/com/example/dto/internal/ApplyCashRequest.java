package com.example.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyCashRequest(
        UUID userKeycloakId,
        BigDecimal delta,
        String idempotencyKey
) {}