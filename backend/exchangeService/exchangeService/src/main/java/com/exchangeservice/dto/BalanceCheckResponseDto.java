package com.exchangeservice.dto;

public class BalanceCheckResponseDto {
    private boolean hasEnoughBalance;
    private String correlationId;
    
    // Constructors, getters ve setters
    public BalanceCheckResponseDto() {}

    public BalanceCheckResponseDto(boolean hasEnoughBalance) {
        this.hasEnoughBalance = hasEnoughBalance;
        this.correlationId = null; // veya UUID.randomUUID().toString() gibi bir değer
    }
    
    public BalanceCheckResponseDto(boolean hasEnoughBalance, String correlationId) {
        this.hasEnoughBalance = hasEnoughBalance;
        this.correlationId = correlationId;
    }
    
    public boolean isHasEnoughBalance() { return hasEnoughBalance; }
    public void setHasEnoughBalance(boolean hasEnoughBalance) { this.hasEnoughBalance = hasEnoughBalance; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}

