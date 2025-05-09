package com.exchangeservice.dto;

import java.math.BigDecimal;

public class ExchangeRequestDto {
    private String username;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private String transactionType;
    private Long accountId; 

    public ExchangeRequestDto() {}

    public ExchangeRequestDto(String username, String fromCurrency, String toCurrency, 
                             BigDecimal amount, String transactionType, Long accountId) {
        this.username = username;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
        this.transactionType = transactionType;
        this.accountId = accountId;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
}
