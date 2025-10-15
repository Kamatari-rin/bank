package com.example.service;

import com.example.dto.CashRequest;
import com.example.dto.CashResult;

import java.util.UUID;

public interface CashAppService {
    CashResult deposit(UUID userKeycloakId, CashRequest r);
    CashResult withdraw(UUID userKeycloakId, CashRequest r);
}