package com.example.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferInternalResponse(
        UUID fromAccountId, BigDecimal fromNewBalance,
        UUID toAccountId,   BigDecimal toNewBalance
) {}
