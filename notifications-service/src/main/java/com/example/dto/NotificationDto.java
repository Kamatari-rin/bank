package com.example.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        Instant ts,
        String title,
        String message
) {}