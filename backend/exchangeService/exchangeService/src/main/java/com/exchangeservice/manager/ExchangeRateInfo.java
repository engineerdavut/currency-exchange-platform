package com.exchangeservice.manager;

import java.math.BigDecimal;

public class ExchangeRateInfo {
    private final BigDecimal rate;
    private final OperationType operationType;
    
    public ExchangeRateInfo(BigDecimal rate, OperationType operationType) {
        this.rate = rate;
        this.operationType = operationType;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
}
