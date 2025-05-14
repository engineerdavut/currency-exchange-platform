package com.exchangeservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExchangeResponseDto {
    private String status;
    private String message;
    private BigDecimal executedPrice;
    private LocalDateTime timestamp;
    private BigDecimal fromAmount;
    private String fromCurrency;
    private BigDecimal toAmount;
    private String toCurrency;

    public ExchangeResponseDto() {
    }

    public ExchangeResponseDto(String status, String message, BigDecimal executedPrice, LocalDateTime timestamp,
            BigDecimal fromAmount, String fromCurrency, BigDecimal toAmount, String toCurrency) {
        this.status = status;
        this.message = message;
        this.executedPrice = executedPrice;
        this.timestamp = timestamp;
        this.fromAmount = fromAmount;
        this.fromCurrency = fromCurrency;
        this.toAmount = toAmount;
        this.toCurrency = toCurrency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public void setExecutedPrice(BigDecimal executedPrice) {
        this.executedPrice = executedPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getFromAmount() {
        return fromAmount;
    }

    public void setFromAmount(BigDecimal fromAmount) {
        this.fromAmount = fromAmount;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public BigDecimal getToAmount() {
        return toAmount;
    }

    public void setToAmount(BigDecimal toAmount) {
        this.toAmount = toAmount;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

}