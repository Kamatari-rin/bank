package com.example.dto.exchange;

import com.example.dto.exchange.RateRowMessage;

import java.time.Instant;
import java.util.Map;

public record ExchangeRateMessage(
        String baseCurrency,
        Map<String, RateRowMessage> rates,
        Instant generatedAt
) {}
