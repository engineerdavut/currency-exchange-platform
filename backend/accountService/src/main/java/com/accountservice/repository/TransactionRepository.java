package com.accountservice.repository;

import com.accountservice.entity.Transaction;
import com.accountservice.entity.Account;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop5ByAccountOrderByTimestampDesc(Account account);

    @Query("SELECT t FROM Transaction t WHERE t.account.user.username = :username ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactionsByUsername(@Param("username") String username, Pageable pageable);
}
