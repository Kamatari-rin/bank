package com.example.web;

import com.example.model.RateRow;
import com.example.service.RateStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class ExchangeController {

    private final RateStore store;

    public ExchangeController(RateStore store) {
        this.store = store;
    }

    @GetMapping("/rates")
    public Map<String, RateRow> rates() {
        return store.snapshot();
    }

    public record QuoteResponse(String from, String to, BigDecimal amountFrom, BigDecimal amountTo) {}

    @GetMapping("/quote")
    public QuoteResponse quote(@RequestParam @NotBlank String from,
                               @RequestParam @NotBlank String to,
                               @RequestParam @NotNull BigDecimal amount,
                               JwtAuthenticationToken ignored) {
        var rates = store.snapshot();
        var f = from.toUpperCase();
        var t = to.toUpperCase();
        var fr = rates.get(f);
        var tr = rates.get(t);
        if (fr == null || tr == null) throw new IllegalArgumentException("Unsupported currency");

        BigDecimal inRub = amount.multiply(fr.sell());
        BigDecimal out = inRub.divide(tr.buy(), 2, RoundingMode.HALF_UP);
        return new QuoteResponse(f, t, amount, out);
    }


    public record RatesUpdateRequest(@NotNull Map<String, @Valid RateRow> rates) {}
}
