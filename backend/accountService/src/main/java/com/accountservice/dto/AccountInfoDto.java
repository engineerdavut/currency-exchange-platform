package com.accountservice.dto;

import java.math.BigDecimal;

public class AccountInfoDto {
    private Long accountId;
    private String currencyType;
    private BigDecimal balance;

    public AccountInfoDto() {
    }

    public AccountInfoDto(Long accountId, String currencyType, BigDecimal balance) {
        this.accountId = accountId;
        this.currencyType = currencyType;
        this.balance = balance;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
