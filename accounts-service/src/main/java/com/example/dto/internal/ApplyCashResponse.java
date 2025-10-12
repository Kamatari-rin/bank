package com.example.dto.internal;


import java.math.BigDecimal;
import java.util.UUID;

public record ApplyCashResponse(
        UUID accountId,
        BigDecimal newBalance
) {}