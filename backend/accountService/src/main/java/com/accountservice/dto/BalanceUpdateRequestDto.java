package com.accountservice.dto;

import java.math.BigDecimal;

public class BalanceUpdateRequestDto {
    private String username;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private String transactionId;

    public BalanceUpdateRequestDto() {
    }

    public BalanceUpdateRequestDto(String username, String fromCurrency, String toCurrency,
            BigDecimal fromAmount, BigDecimal toAmount, String transactionId) {
        this.username = username;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.transactionId = transactionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public BigDecimal getFromAmount() {
        return fromAmount;
    }

    public void setFromAmount(BigDecimal fromAmount) {
        this.fromAmount = fromAmount;
    }

    public BigDecimal getToAmount() {
        return toAmount;
    }

    public void setToAmount(BigDecimal toAmount) {
        this.toAmount = toAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
