// src/main/java/com/example/web/PublicBrowseController.java
package com.example.web;

import com.example.dto.publicview.PublicAccountDto;
import com.example.dto.publicview.PublicUserDto;
import com.example.repository.BankAccountRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicBrowseController {

    private final UserRepository users;
    private final BankAccountRepository accounts;

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public List<PublicUserDto> users() {
        return users.findAll().stream()
                .map(u -> new PublicUserDto(u.getId(), u.getKeycloakId(),
                        u.getFirstName(), u.getLastName(), u.getEmail()))
                .toList();
    }

    @GetMapping("/users/{userId}/accounts")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public List<PublicAccountDto> userAccounts(@PathVariable java.util.UUID userId) {
        return accounts.findByUserId(userId).stream()
                .map(a -> new PublicAccountDto(a.getId(), a.getCurrency().name()))
                .toList();
    }
}
