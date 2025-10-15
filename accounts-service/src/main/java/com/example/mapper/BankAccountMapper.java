package com.example.mapper;

import com.example.dto.BankAccountDto;
import com.example.entity.BankAccount;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "userId", source = "user.id")
    BankAccountDto toDto(BankAccount entity);

    List<BankAccountDto> toDtoList(List<BankAccount> entities);
}