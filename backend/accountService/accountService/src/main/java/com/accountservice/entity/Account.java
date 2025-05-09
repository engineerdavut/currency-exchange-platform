package com.accountservice.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currencyType;
    
    @Column(nullable = false)
    private BigDecimal balance;
    
    public Account() {}
    
    public Account(User user, CurrencyType currencyType, BigDecimal balance) {
        this.user = user;
        this.currencyType = currencyType;
        this.balance = balance;
    }
    
    public Long getId() { return id; }
    public User getUser() { return user; }
    public CurrencyType getCurrencyType() { return currencyType; }
    public BigDecimal getBalance() { return balance; }
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setCurrencyType(CurrencyType currencyType) { this.currencyType = currencyType; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}

