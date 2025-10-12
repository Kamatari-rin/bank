package com.example.dto.publicview;

import java.util.UUID;

public record PublicAccountDto(
        UUID id,
        String currency       // RUB | USD | CNY
) {}