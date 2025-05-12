package com.accountservice.dto;

import java.math.BigDecimal;

public class BalanceCheckRequestDto {
    private String username;
    private String currency;
    private BigDecimal amount;
    private String correlationId;
    
    public BalanceCheckRequestDto() {}
    
    public BalanceCheckRequestDto(String username, String currency, BigDecimal amount, String correlationId) {
        this.username = username;
        this.currency = currency;
        this.amount = amount;
        this.correlationId = correlationId;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
