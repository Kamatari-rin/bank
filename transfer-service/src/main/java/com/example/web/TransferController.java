package com.example.web;

import com.example.dto.TransferResult;
import com.example.service.TransferAppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {

    private final TransferAppService service;

    public record TransferCommand(
            @NotNull UUID fromAccountId,
            @NotNull @Pattern(regexp = "RUB|USD|CNY") String fromCurrency,
            @NotNull UUID toAccountId,
            @NotNull @Pattern(regexp = "RUB|USD|CNY") String toCurrency,
            @NotNull @Positive BigDecimal amount
    ) {}

    @PostMapping("/own")
    @ResponseStatus(HttpStatus.OK)
    public TransferResult transferOwn(@Valid @RequestBody TransferCommand r,
                                      JwtAuthenticationToken auth) {
        UUID kid = UUID.fromString(auth.getToken().getClaimAsString("sub"));
        return service.transferOwn(
                kid,
                r.fromAccountId(), r.fromCurrency(),
                r.toAccountId(),   r.toCurrency(),
                r.amount()
        );
    }

    @PostMapping("/external")
    @ResponseStatus(HttpStatus.OK)
    public TransferResult transferExternal(@Valid @RequestBody TransferCommand r,
                                           JwtAuthenticationToken auth) {
        // Тот же вызов: сервис сам поймёт own/external, проверив владельцев.
        UUID kid = UUID.fromString(auth.getToken().getClaimAsString("sub"));
        return service.transferOwn(
                kid,
                r.fromAccountId(), r.fromCurrency(),
                r.toAccountId(),   r.toCurrency(),
                r.amount()
        );
    }
}
