package com.example.dto;

import java.util.UUID;

// Команда для внутреннего POST из других сервисов
public record NotifyCommand(
        UUID userId,
        String title,
        String message
) {}