package com.example.dto.publicview;

import java.util.UUID;

public record PublicUserDto(
        UUID id,
        UUID keycloakId,
        String firstName,
        String lastName,
        String email
) {}