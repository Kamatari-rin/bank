package com.example.dto.publicview;

import java.util.UUID;

public record PublicUserDto(
        UUID id,              // внутренний id (UUID из таблицы users)
        UUID keycloakId,      // sub в токене
        String firstName,
        String lastName,
        String email
) {}