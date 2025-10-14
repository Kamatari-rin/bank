package com.example.dto;

import java.util.UUID;

public record NotifyCommand(
        UUID userId,
        String title,
        String message
) {}