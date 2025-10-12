package com.example.mapper;

import com.example.dto.UserProfileDto;
import com.example.entity.BankAccount;
import com.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BankAccountMapper.class})
public interface UserProfileMapper {

    @Mapping(target = "user", expression = "java(userMapper.toDto(user))")
    @Mapping(target = "accounts", expression = "java(bankAccountMapper.toDtoList(accounts))")
    UserProfileDto toProfile(User user, List<BankAccount> accounts, UserMapper userMapper, BankAccountMapper bankAccountMapper);
}