package com.accountservice.manager;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletManager {

    @Autowired
    private AccountService accountService;

    public List<AccountInfoDto> getWallet(String username) {

        return accountService.getAccountInfo(username);
    }
}
