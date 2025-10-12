package com.example.repository;

import com.example.entity.BankAccount;
import com.example.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    List<BankAccount> findByUserId(UUID userId);
    Optional<BankAccount> findByUserIdAndCurrency(UUID userId, Currency currency);
    boolean existsByUserIdAndCurrency(UUID userId, Currency currency);

    @Query("""
           select a from BankAccount a
           join fetch a.user u
           where a.id = :id
           """)
    Optional<BankAccount> findWithUserById(UUID id);
}