package com.example.dto;

import com.example.validation.Adult;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegistrationRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotNull @Adult LocalDate birthDate
) {
    /** Возвращает копию, но с нормализованным email */
    public RegistrationRequest withEmail(String normalizedEmail) {
        return new RegistrationRequest(username, password, firstName, lastName, normalizedEmail, birthDate);
    }
}
