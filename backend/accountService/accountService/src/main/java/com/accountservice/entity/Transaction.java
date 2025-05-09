package com.accountservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String transactionType; 
    
    @Column
    private String exchangeType; 
    
    @Column(length = 50)
    private String relatedCurrency; 
    
    @Column
    private Long relatedTransactionId; 

    public Transaction() {}
    
    public Transaction(Account account, LocalDateTime timestamp, BigDecimal amount, 
                      String description, String transactionType) {
        this.account = account;
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
    }
    
    public Transaction(Account account, LocalDateTime timestamp, BigDecimal amount, 
                      String description, String transactionType, String exchangeType, 
                      String relatedCurrency, Long relatedTransactionId) {
        this.account = account;
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
        this.exchangeType = exchangeType;
        this.relatedCurrency = relatedCurrency;
        this.relatedTransactionId = relatedTransactionId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getExchangeType() { return exchangeType; }
    public void setExchangeType(String exchangeType) { this.exchangeType = exchangeType; }
    public String getRelatedCurrency() { return relatedCurrency; }
    public void setRelatedCurrency(String relatedCurrency) { this.relatedCurrency = relatedCurrency; }
    public Long getRelatedTransactionId() { return relatedTransactionId; }
    public void setRelatedTransactionId(Long relatedTransactionId) { this.relatedTransactionId = relatedTransactionId; }
}