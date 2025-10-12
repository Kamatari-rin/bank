package com.example.dto.internal;

import java.util.UUID;

public record AccountInfoInternal(
        UUID accountId,
        UUID ownerKeycloakId,
        String currency
) {}
