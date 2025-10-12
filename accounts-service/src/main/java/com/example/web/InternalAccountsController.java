package com.example.web;

import com.example.dto.internal.AccountInfoInternal;
import com.example.dto.internal.ApplyCashRequest;
import com.example.dto.internal.ApplyCashResponse;
import com.example.entity.BankAccount;
import com.example.entity.User;
import com.example.exception.NotFoundException;
import com.example.repository.BankAccountRepository;
import com.example.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalAccountsController {

    private final UserRepository users;
    private final BankAccountRepository accounts;

    @PostMapping("/accounts/{accountId}/apply-cash")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public ApplyCashResponse applyCash(@PathVariable UUID accountId,
                                       @Valid @RequestBody ApplyCashRequest req) {
        User user = users.findByKeycloakId(req.userKeycloakId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        BankAccount acc = accounts.findById(accountId)
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal newBalance = acc.getBalance().add(req.delta());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.example.exception.ValidationException("Insufficient funds");
        }
        acc.setBalance(newBalance);

        return new ApplyCashResponse(acc.getId(), acc.getBalance());
    }

    @GetMapping("/account-info/{accountId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('transfer-writer','cash-writer')")
    @Transactional
    public AccountInfoInternal accountInfo(@PathVariable UUID accountId) {
        BankAccount a = accounts.findWithUserById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return new AccountInfoInternal(
                a.getId(),
                a.getUser().getKeycloakId(),
                a.getCurrency().name()
        );
    }
}
