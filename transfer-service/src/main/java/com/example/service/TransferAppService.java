package com.example.service;

import com.example.dto.TransferResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferAppService {
    TransferResult transferOwn(UUID keycloakId,
                               UUID fromAccountId, String fromCurrency,
                               UUID toAccountId,   String toCurrency,
                               BigDecimal amount);
}
