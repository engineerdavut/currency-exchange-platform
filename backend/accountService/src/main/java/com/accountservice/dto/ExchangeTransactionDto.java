package com.accountservice.dto;

import java.math.BigDecimal;

public class ExchangeTransactionDto {
    private String username;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;

    public ExchangeTransactionDto() {
    }

    public ExchangeTransactionDto(String username, String fromCurrency, String toCurrency,
            BigDecimal fromAmount, BigDecimal toAmount) {
        this.username = username;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
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
}