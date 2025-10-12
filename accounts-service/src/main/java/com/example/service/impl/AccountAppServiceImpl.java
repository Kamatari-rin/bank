package com.example.service.impl;

import com.example.dto.*;
import com.example.entity.BankAccount;
import com.example.entity.User;
import com.example.exception.ConflictException;
import com.example.exception.NonZeroBalanceDeletionException;
import com.example.exception.NotFoundException;
import com.example.integration.keycloak.KeycloakAdminClient;
import com.example.mapper.BankAccountMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.UserProfileMapper;
import com.example.repository.BankAccountRepository;
import com.example.repository.UserRepository;
import com.example.service.AccountAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountAppServiceImpl implements AccountAppService {

    private final UserRepository users;
    private final BankAccountRepository accounts;
    private final UserMapper userMapper;
    private final BankAccountMapper accountMapper;
    private final UserProfileMapper profileMapper;
    private final KeycloakAdminClient kc;

    @Override
    public UserDto register(RegistrationRequest r, UUID ignored) {
        final String email = r.email().toLowerCase();
        if (users.existsByEmail(email)) {
            log.info("register: email already used email={}", email);
            throw new ConflictException("Email is already used");
        }

        log.debug("register: creating KC user username={} firstName={} lastName={} email={}",
                r.username(), r.firstName(), r.lastName(), email);

        UUID keycloakId = kc.createUser(r.username(), r.firstName(), r.lastName(), email, true);
        kc.setPassword(keycloakId, r.password());

        log.info("register: KC user created kcId={} username={} email={}", keycloakId, r.username(), email);

        UserDto dto = createLocalProfile(keycloakId, r.withEmail(email));
        log.info("register: local profile saved userId={} kcId={} email={}", dto.id(), dto.keycloakId(), dto.email());
        return dto;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected UserDto createLocalProfile(UUID keycloakId, RegistrationRequest r) {
        var user = User.builder()
                .keycloakId(keycloakId)
                .firstName(r.firstName())
                .lastName(r.lastName())
                .email(r.email())
                .birthDate(r.birthDate())
                .build();
        var saved = users.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto profile(UUID keycloakId) {
        var user = users.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        log.debug("profile: kcId={} userId={}", keycloakId, user.getId());
        var accs = accounts.findByUserId(user.getId());
        return profileMapper.toProfile(user, accs, userMapper, accountMapper);
    }

    @Override
    public UserDto updateUser(UUID keycloakId, UpdateUserRequest r) {
        var user = users.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        final String email = r.email().toLowerCase();
        if (users.existsByEmailAndIdNot(email, user.getId())) {
            log.info("updateUser: email in use by another user kcId={} email={}", keycloakId, email);
            throw new ConflictException("Email is already used by another user");
        }
        user.setFirstName(r.firstName());
        user.setLastName(r.lastName());
        user.setEmail(email);
        user.setBirthDate(r.birthDate());
        var dto = userMapper.toDto(user);
        log.info("updateUser: updated kcId={} email={}", keycloakId, email);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDto> listAccounts(UUID keycloakId) {
        var user = users.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        var list = accounts.findByUserId(user.getId());
        log.debug("listAccounts: kcId={} userId={} size={}", keycloakId, user.getId(), list.size());
        return accountMapper.toDtoList(list);
    }

    @Override
    public BankAccountDto createAccount(UUID keycloakId, CreateAccountRequest r) {
        var user = users.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        accounts.findByUserIdAndCurrency(user.getId(), r.currency())
                .ifPresent(a -> { throw new ConflictException("Account for currency already exists"); });

        var entity = BankAccount.builder()
                .user(user)
                .currency(r.currency())
                .balance(BigDecimal.ZERO)
                .build();

        var saved = accounts.save(entity);
        log.info("createAccount: kcId={} userId={} accountId={} currency={}",
                keycloakId, user.getId(), saved.getId(), saved.getCurrency());
        return accountMapper.toDto(saved);
    }

    @Override
    public void deleteAccount(UUID keycloakId, UUID accountId) {
        var user = users.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        var acc = accounts.findById(accountId)
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if (acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            log.info("deleteAccount: non-zero balance kcId={} accountId={} balance={}",
                    keycloakId, accountId, acc.getBalance());
            throw new NonZeroBalanceDeletionException("Cannot delete account with non-zero balance");
        }
        accounts.delete(acc);
        log.info("deleteAccount: deleted kcId={} accountId={}", keycloakId, accountId);
    }
}
