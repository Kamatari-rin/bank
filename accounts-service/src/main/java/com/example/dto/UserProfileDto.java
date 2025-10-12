package com.example.dto;

import java.util.List;

public record UserProfileDto(UserDto user, List<BankAccountDto> accounts) {}