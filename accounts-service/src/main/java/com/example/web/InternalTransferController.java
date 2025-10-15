package com.example.web;

import com.example.dto.internal.TransferInternalRequest;
import com.example.dto.internal.TransferInternalResponse;
import com.example.entity.BankAccount;
import com.example.entity.User;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.repository.BankAccountRepository;
import com.example.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalTransferController {

    private final UserRepository users;
    private final BankAccountRepository accounts;

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public TransferInternalResponse transfer(@Valid @RequestBody TransferInternalRequest r) {
        // 1) Отправитель должен существовать
        User sender = users.findByKeycloakId(r.senderKeycloakId())
                .orElseThrow(() -> new NotFoundException("Sender user not found"));

        // 2) Исходный счёт должен принадлежать отправителю
        BankAccount from = accounts.findById(r.fromAccountId())
                .filter(a -> a.getUser().getId().equals(sender.getId()))
                .orElseThrow(() -> new NotFoundException("Source account not found or not owned by sender"));

        // 3) Целевой счёт может принадлежать ЛЮБОМу пользователю (внешний перевод)
        BankAccount to = accounts.findById(r.toAccountId())
                .orElseThrow(() -> new NotFoundException("Target account not found"));

        // 4) Валидация сумм
        if (r.amountFrom().signum() <= 0 || r.amountTo().signum() <= 0) {
            throw new ValidationException("Amounts must be positive");
        }
        if (from.getId().equals(to.getId())) {
            throw new ValidationException("Cannot transfer to the same account");
        }

        // 5) Достаточность средств
        if (from.getBalance().compareTo(r.amountFrom()) < 0) {
            throw new ValidationException("Insufficient funds");
        }

        // 6) Проводка
        from.setBalance(from.getBalance().subtract(r.amountFrom()));
        to.setBalance(to.getBalance().add(r.amountTo()));

        log.info("internal transfer: senderKcId={} from={}(-{}) -> to={} (+{}). New balances: {} / {}",
                r.senderKeycloakId(),
                from.getId(), r.amountFrom(),
                to.getId(),   r.amountTo(),
                from.getBalance(), to.getBalance());

        // @Transactional обеспечит flush
        return new TransferInternalResponse(
                from.getId(), from.getBalance(),
                to.getId(),   to.getBalance()
        );
    }
}
