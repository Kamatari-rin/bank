package com.example.service.impl;

import com.example.dto.CashRequest;
import com.example.dto.CashResult;
import com.example.dto.internal.ApplyCashRequest;
import com.example.dto.internal.ApplyCashResponse;
import com.example.dto.internal.BlockerDtos;
import com.example.exception.ForbiddenOperationException;
import com.example.integration.AccountsClient;
import com.example.integration.BlockerClient;
import com.example.integration.NotificationsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashAppServiceImpl implements com.example.service.CashAppService {

    private final AccountsClient accounts;
    private final BlockerClient blocker;
    private final NotificationsClient notifications;

    @Override
    public CashResult deposit(UUID userKeycloakId, CashRequest r) {
        var amount = r.amount(); // > 0
        log.info("cash.deposit start kcId={} accountId={} amount={}", userKeycloakId, r.accountId(), amount);

        // 1) антифрод
        var check = blocker.check(new BlockerDtos.CheckRequest(
                BlockerDtos.OperationType.DEPOSIT,
                userKeycloakId,
                r.accountId(),   // source
                null,            // target
                amount,
                Instant.now()
        ));
        if (!check.allowed()) {
            log.warn("cash.deposit blocked kcId={} accountId={} reason={} score={}",
                    userKeycloakId, r.accountId(), check.reason(), check.score());
            throw new ForbiddenOperationException("Blocked by anti-fraud: " + check.reason());
        }

        // 2) применяем изменение баланса
        ApplyCashResponse resp = accounts.applyCash(
                r.accountId(),
                new ApplyCashRequest(userKeycloakId, amount, r.idempotencyKey())
        );

        // 3) уведомление
        notifications.notify(
                userKeycloakId,
                "Пополнение счёта",
                "На счёт %s зачислено %s. Новый баланс: %s"
                        .formatted(r.accountId(), amount.toPlainString(), resp.newBalance().toPlainString())
        );

        log.info("cash.deposit ok kcId={} accountId={} newBalance={}", userKeycloakId, resp.accountId(), resp.newBalance());
        return new CashResult(resp.accountId(), resp.newBalance());
    }

    @Override
    public CashResult withdraw(UUID userKeycloakId, CashRequest r) {
        var amount = r.amount();         // > 0 по контракту
        var delta  = amount.negate();    // < 0 для списания
        log.info("cash.withdraw start kcId={} accountId={} amount={}", userKeycloakId, r.accountId(), amount);

        // 1) антифрод
        var check = blocker.check(new BlockerDtos.CheckRequest(
                BlockerDtos.OperationType.WITHDRAW,
                userKeycloakId,
                r.accountId(),   // source
                null,            // target
                amount,
                Instant.now()
        ));
        if (!check.allowed()) {
            log.warn("cash.withdraw blocked kcId={} accountId={} reason={} score={}",
                    userKeycloakId, r.accountId(), check.reason(), check.score());
            throw new ForbiddenOperationException("Blocked by anti-fraud: " + check.reason());
        }

        // 2) применяем изменение баланса
        ApplyCashResponse resp = accounts.applyCash(
                r.accountId(),
                new ApplyCashRequest(userKeycloakId, delta, r.idempotencyKey())
        );

        // 3) уведомление
        notifications.notify(
                userKeycloakId,
                "Списание со счёта",
                "Со счёта %s списано %s. Новый баланс: %s"
                        .formatted(r.accountId(), amount.toPlainString(), resp.newBalance().toPlainString())
        );

        log.info("cash.withdraw ok kcId={} accountId={} newBalance={}", userKeycloakId, resp.accountId(), resp.newBalance());
        return new CashResult(resp.accountId(), resp.newBalance());
    }
}
