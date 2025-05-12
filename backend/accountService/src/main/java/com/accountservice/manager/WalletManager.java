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

    // Kullanıcının tüm hesap bakiyelerini getirir
    public List<AccountInfoDto> getWallet(String username) {
        // Bu örnekte, mevcut getAccountInfo metodu tüm hesapları döndürdüğü varsayılıyor.
        return accountService.getAccountInfo(username);
    }
}
