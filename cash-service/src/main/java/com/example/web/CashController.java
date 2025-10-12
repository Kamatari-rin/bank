package com.example.web;

import com.example.dto.CashRequest;
import com.example.dto.CashResult;
import com.example.service.CashAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CashController {

    private final CashAppService service;

    private UUID keycloakId(JwtAuthenticationToken auth) {
        return UUID.fromString(auth.getToken().getClaimAsString("sub"));
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.OK)
    public CashResult deposit(@Valid @RequestBody CashRequest req, JwtAuthenticationToken auth) {
        return service.deposit(keycloakId(auth), req);
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    public CashResult withdraw(@Valid @RequestBody CashRequest req, JwtAuthenticationToken auth) {
        return service.withdraw(keycloakId(auth), req);
    }
}