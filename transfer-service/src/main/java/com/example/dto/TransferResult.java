package com.example.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResult(
        UUID fromAccountId, BigDecimal fromNewBalance,
        UUID toAccountId,   BigDecimal toNewBalance,
        BigDecimal amountFrom, BigDecimal amountTo
) {}
