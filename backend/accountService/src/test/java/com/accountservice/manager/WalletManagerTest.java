package com.accountservice.manager;

import com.accountservice.dto.AccountInfoDto;
import com.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletManagerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private WalletManager walletManager;

    @Test
    void getWallet_ShouldReturnAccountInfo() {
        // Arrange
        String username = "testUser";
        List<AccountInfoDto> expectedWallet = Arrays.asList(
            new AccountInfoDto(1L, "TRY", new BigDecimal("1000")),
            new AccountInfoDto(2L, "USD", new BigDecimal("100"))
        );
        when(accountService.getAccountInfo(username)).thenReturn(expectedWallet);

        // Act
        List<AccountInfoDto> result = walletManager.getWallet(username);

        // Assert
        assertEquals(expectedWallet, result);
        verify(accountService).getAccountInfo(username);
    }
}

