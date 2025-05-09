package com.accountservice.repository;

import com.accountservice.entity.Account;
import com.accountservice.entity.User;
import com.accountservice.entity.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserAndCurrencyType(User user, CurrencyType currencyType);
    List<Account> findByUser(User user);
}

