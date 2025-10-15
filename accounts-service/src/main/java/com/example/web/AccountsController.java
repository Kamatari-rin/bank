package com.example.web;

import com.example.dto.*;
import com.example.security.CurrentUser;
import com.example.service.AccountAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountsController {

    private final AccountAppService service;
    private final CurrentUser current;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegistrationRequest req) {
        return service.register(req, null);
    }

    @GetMapping("/me")
    public UserProfileDto me(JwtAuthenticationToken auth) {
        UUID kid = current.keycloakId(auth);
        return service.profile(kid);
    }

    @PutMapping("/me")
    public UserDto updateMe(@Valid @RequestBody UpdateUserRequest req,
                            JwtAuthenticationToken auth) {
        return service.updateUser(current.keycloakId(auth), req);
    }

    @GetMapping("/me/accounts")
    public List<BankAccountDto> myAccounts(JwtAuthenticationToken auth) {
        return service.listAccounts(current.keycloakId(auth));
    }

    @PostMapping("/me/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountDto createAccount(@Valid @RequestBody CreateAccountRequest req,
                                        JwtAuthenticationToken auth) {
        return service.createAccount(current.keycloakId(auth), req);
    }

    @DeleteMapping("/me/accounts/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable UUID accountId, JwtAuthenticationToken auth) {
        service.deleteAccount(current.keycloakId(auth), accountId);
    }
}
