package com.example.dto.exchange;

import java.math.BigDecimal;

public record RateRowMessage(
        String currency,
        BigDecimal buy,
        BigDecimal sell
) {}