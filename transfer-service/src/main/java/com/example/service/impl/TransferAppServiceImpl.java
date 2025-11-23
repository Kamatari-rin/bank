package com.example.service.impl;

import com.example.dto.TransferResult;
import com.example.dto.internal.BlockerDtos;
import com.example.dto.internal.TransferInternalRequest;
import com.example.exception.ForbiddenOperationException;
import com.example.exception.ValidationException;
import com.example.integration.AccountsClient;
import com.example.integration.BlockerClient;
import com.example.integration.ExchangeClient;
import com.example.integration.NotificationsClient;
import com.example.metrics.TransferMetrics;
import com.example.service.TransferAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferAppServiceImpl implements TransferAppService {

    private final ExchangeClient exchange;
    private final AccountsClient accounts;
    private final BlockerClient blocker;
    private final NotificationsClient notifications;
    private final TransferMetrics metrics;  // <<< ДОБАВЛЕНО

    @Override
    public TransferResult transferOwn(UUID keycloakId,
                                      UUID fromAccountId, String ignoredFromCurrency,
                                      UUID toAccountId,   String ignoredToCurrency,
                                      BigDecimal amount) {

        String sender = keycloakId.toString();
        String fromAcc = fromAccountId.toString();
        String toAcc   = toAccountId.toString();

        if (amount == null || amount.signum() <= 0) {
            metrics.transferFailure("own", sender, fromAcc, toAcc, "invalid_amount");
            throw new ValidationException("Amount must be > 0");
        }

        log.info("transfer.own start kcId={} from={} to={} amount={}",
                keycloakId, fromAccountId, toAccountId, amount);

        // 1) верификация аккаунтов
        var fromInfo = accounts.accountInfo(fromAccountId);
        var toInfo   = accounts.accountInfo(toAccountId);

        boolean sameOwner = fromInfo.ownerKeycloakId().equals(toInfo.ownerKeycloakId());
        String type = sameOwner ? "own" : "external";

        if (!fromInfo.ownerKeycloakId().equals(keycloakId)) {
            metrics.transferFailure(type, sender, fromAcc, toAcc, "not_owner");
            throw new ForbiddenOperationException("Source account is not owned by caller");
        }

        // 2) антифрод
        var check = blocker.check(new BlockerDtos.CheckRequest(
                sameOwner ? BlockerDtos.OperationType.TRANSFER_OWN : BlockerDtos.OperationType.TRANSFER_EXTERNAL,
                keycloakId,
                fromAccountId,
                toAccountId,
                amount,
                Instant.now()
        ));

        if (!check.allowed()) {
            metrics.transferBlocked(type, sender, fromAcc, toAcc, check.reason());
            log.warn("transfer blocked: reason={} score={}", check.reason(), check.score());
            throw new ForbiddenOperationException("Blocked by anti-fraud: " + check.reason());
        }

        // 3) конвертация
        String fromCur = fromInfo.currency();
        String toCur   = toInfo.currency();

        BigDecimal amountTo = amount;
        if (!fromCur.equalsIgnoreCase(toCur)) {
            Map<String, Map<String, Object>> rates = exchange.getRates();
            amountTo = exchange.convert(amount, fromCur, toCur, rates);
        }

        // 4) транзакция через Accounts
        var req = new TransferInternalRequest(
                keycloakId, fromAccountId, toAccountId, amount, amountTo
        );
        var resp = accounts.transfer(req);

        // успех → увеличиваем метрику
        metrics.transferSuccess(type, sender, fromAcc, toAcc);

        // 5) уведомления
        String typeOut = sameOwner ? "TRANSFER_OWN" : "TRANSFER_EXTERNAL";
        String titleOut = sameOwner ? "Перевод между своими счетами" : "Перевод отправлен";
        String msgOut = "Со счёта %s отправлено %s %s. Новый баланс: %s"
                .formatted(fromAccountId, amount, fromCur, resp.fromNewBalance());

        notifications.notify(keycloakId, typeOut, titleOut, msgOut);

        if (!sameOwner) {
            notifications.notify(
                    toInfo.ownerKeycloakId(),
                    "TRANSFER_INCOMING",
                    "Получен перевод",
                    "На счёт %s поступило %s %s. Новый баланс: %s"
                            .formatted(toAccountId, amountTo, toCur, resp.toNewBalance())
            );
        }

        log.info("transfer.own ok kcId={} from={} to={} amount={} amountTo={} fromBal={} toBal={}",
                keycloakId, fromAccountId, toAccountId,
                amount, amountTo, resp.fromNewBalance(), resp.toNewBalance());

        return new TransferResult(
                resp.fromAccountId(), resp.fromNewBalance(),
                resp.toAccountId(),   resp.toNewBalance(),
                amount, amountTo
        );
    }
}
