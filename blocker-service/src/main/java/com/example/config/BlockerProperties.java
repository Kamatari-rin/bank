package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blocker")
public record BlockerProperties(
        int randomBlockPercent,
        int nightStartHour,
        int nightEndHour,
        long nightAmountThreshold,
        long maxAmount
) {}
