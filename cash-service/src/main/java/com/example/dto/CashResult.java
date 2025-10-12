package com.example.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CashResult(
        UUID accountId,
        BigDecimal newBalance
) {}