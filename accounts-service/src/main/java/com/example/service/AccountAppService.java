package com.example.service;

import com.example.dto.*;

import java.util.List;
import java.util.UUID;

public interface AccountAppService {

    UserDto register(RegistrationRequest r, UUID keycloakId);
    UserProfileDto profile(UUID keycloakId);
    UserDto updateUser(UUID keycloakId, UpdateUserRequest r);
    List<BankAccountDto> listAccounts(UUID keycloakId);
    BankAccountDto createAccount(UUID keycloakId, CreateAccountRequest r);
    void deleteAccount(UUID keycloakId, UUID accountId);

}
