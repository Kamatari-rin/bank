package com.example.model;

import java.math.BigDecimal;

public record RateRow(String currency, BigDecimal buy, BigDecimal sell) {}