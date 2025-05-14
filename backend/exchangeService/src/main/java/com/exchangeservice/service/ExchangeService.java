package com.exchangeservice.service;

import com.exchangeservice.config.RabbitMQConfig;
import com.exchangeservice.dto.*;
import com.exchangeservice.entity.ExchangeTransaction;
import com.exchangeservice.exception.ExchangeException;
import com.exchangeservice.exception.InsufficientAmountException;
import com.exchangeservice.exception.InsufficientBalanceException;
import com.exchangeservice.manager.ExchangeRateInfo;
import com.exchangeservice.manager.OperationType;
import com.exchangeservice.manager.PriceManager;
import com.exchangeservice.messaging.RabbitMQListener;
import com.exchangeservice.repository.ExchangeTransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ExchangeService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    
    private final PriceManager goldPriceManager;
    private final PriceManager currencyPriceManager;
    private final ExchangeTransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQListener rabbitMQListener;

    public ExchangeService(
        @Qualifier("APILayerManager") PriceManager goldPriceManager,
        @Qualifier("exchangeRateAPIManager") PriceManager currencyPriceManager,
        ExchangeTransactionRepository transactionRepository,
        RabbitTemplate rabbitTemplate,
        RabbitMQListener rabbitMQListener) {
        this.goldPriceManager = goldPriceManager;
        this.currencyPriceManager = currencyPriceManager;
        this.transactionRepository = transactionRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQListener = rabbitMQListener;
    }
    

    public ExchangeResponseDto processExchange(String username, ExchangeRequestDto request) throws Exception {
        String fromCurrency = request.getFromCurrency().toUpperCase();
        String toCurrency = request.getToCurrency().toUpperCase();
        BigDecimal amount = request.getAmount();
        
        logger.info("Processing exchange request: {} {} to {} for user {}", 
                amount, fromCurrency, toCurrency, username);
    
        try {
            PriceManager selectedManager = selectPriceManager(fromCurrency, toCurrency);
            
            ExchangeRateInfo rateInfo = getRateInfo(selectedManager, fromCurrency, toCurrency);
            
            ConversionResult conversionResult = calculateConversion(amount, rateInfo, fromCurrency, toCurrency);
            
            checkUserBalance(username, fromCurrency, conversionResult.getActualCost());
            
            updateUserBalance(username, fromCurrency, toCurrency, 
                    conversionResult.getActualCost(), conversionResult.getConvertedAmount());
            

            saveTransaction(request.getAccountId(), fromCurrency, toCurrency,
                    conversionResult.getActualCost(), conversionResult.getConvertedAmount(), 
                    request.getTransactionType());
            
            return createSuccessResponse(request, rateInfo.getRate(), conversionResult);
            
        } catch (Exception e) {
            logger.error("Exchange process failed: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        }
    }
    
    private PriceManager selectPriceManager(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals("GOLD") || toCurrency.equals("GOLD")) {
            return goldPriceManager; 
        } else {
            return currencyPriceManager; 
        }
    }
    

    private ExchangeRateInfo getRateInfo(PriceManager manager, String fromCurrency, String toCurrency) throws Exception {
        return manager.getExchangeRateInfo(fromCurrency, toCurrency);
    }
    

    private ConversionResult calculateConversion(BigDecimal amount, ExchangeRateInfo rateInfo, 
                                              String fromCurrency, String toCurrency) throws InsufficientAmountException {
        BigDecimal rate = rateInfo.getRate();
        BigDecimal convertedAmount;
        BigDecimal actualCost = amount;
        
        if (toCurrency.equals("GOLD")) {
            return calculateGoldPurchase(amount, rate, rateInfo.getOperationType(), fromCurrency);
        } else if (fromCurrency.equals("GOLD")) {
            return calculateGoldSale(amount, rate, rateInfo.getOperationType(), toCurrency);
        } else {
            convertedAmount = calculateStandardConversion(amount, rate, rateInfo.getOperationType());
            return new ConversionResult(convertedAmount, actualCost);
        }
    }
    
    private ConversionResult calculateGoldPurchase(BigDecimal amount, BigDecimal rate, 
                                              OperationType operationType, String fromCurrency) throws InsufficientAmountException {
        BigDecimal exactGoldAmount;
        if (operationType == OperationType.DIVIDE) {
            exactGoldAmount = amount.divide(rate, 4, RoundingMode.HALF_UP);
        } else {
            exactGoldAmount = amount.multiply(rate);
        }
        

        BigDecimal wholeGrams = exactGoldAmount.setScale(0, RoundingMode.DOWN);
        

        if (wholeGrams.compareTo(BigDecimal.ONE) < 0) {
            throw new InsufficientAmountException("Minimum gold purchase amount is 1 gram. Your amount: " 
                + exactGoldAmount.toPlainString() + " grams");
        }
        

        BigDecimal actualCost;
        if (operationType == OperationType.DIVIDE) {
            actualCost = wholeGrams.multiply(rate);
        } else {
            actualCost = wholeGrams.divide(rate, 2, RoundingMode.HALF_UP);
        }
        
        logger.info("Gold purchase: {} {} can buy {} grams, actual cost: {} {}", 
                amount, fromCurrency, wholeGrams, actualCost, fromCurrency);
        
        return new ConversionResult(wholeGrams, actualCost);
    }
    

    private ConversionResult calculateGoldSale(BigDecimal goldAmount, BigDecimal rate, 
                                           OperationType operationType, String toCurrency) {
        BigDecimal convertedAmount;
        if (operationType == OperationType.MULTIPLY) {
            convertedAmount = goldAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } else {
            convertedAmount = goldAmount.divide(rate, 2, RoundingMode.HALF_UP);
        }
        
        logger.info("Gold sale: {} GOLD sold for {} {}", goldAmount, convertedAmount, toCurrency);
        
        return new ConversionResult(convertedAmount, goldAmount);
    }

    private BigDecimal calculateStandardConversion(BigDecimal amount, BigDecimal rate, OperationType operationType) {
        if (operationType == OperationType.MULTIPLY) {
            logger.info("Multiplying {} by rate {}", amount, rate);
            return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } else {
            logger.info("Dividing {} by rate {}", amount, rate);
            return amount.divide(rate, 2, RoundingMode.HALF_UP);
        }
    }
    


    private void checkUserBalance(String username, String currency, BigDecimal amount) {
        String correlationId = UUID.randomUUID().toString();
        

        BalanceCheckRequestDto balanceRequest = new BalanceCheckRequestDto(
            username, currency, amount, correlationId);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.BALANCE_CHECK_EXCHANGE,
            RabbitMQConfig.BALANCE_CHECK_ROUTING_KEY,
            balanceRequest);
        
        logger.info("Sent balance check request with correlation ID: {}", correlationId);
        

        BalanceCheckResponseDto balanceResponse = null;
        try {
            balanceResponse = rabbitMQListener.getBalanceResponse(correlationId, 10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if (balanceResponse == null) {
            logger.error("Balance check timed out for correlation ID: {}", correlationId);
            throw new ExchangeException("Balance check timed out");
        }
        
        if (!balanceResponse.isHasEnoughBalance()) {
            logger.warn("Insufficient balance for user {} in {}. Required: {}, Available: {}", 
                username, currency, amount, "Unknown");
            throw new InsufficientBalanceException("Insufficient balance in " + currency);
        }
        
        logger.info("Balance check successful for user {}", username);
    }
    

    private void updateUserBalance(String username, String fromCurrency, String toCurrency, 
                                 BigDecimal fromAmount, BigDecimal toAmount) {
        String transactionId = UUID.randomUUID().toString();
        

        BalanceUpdateRequestDto updateRequest = new BalanceUpdateRequestDto(
            username, fromCurrency, toCurrency, fromAmount, toAmount, transactionId);
        

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.BALANCE_UPDATE_EXCHANGE,
            RabbitMQConfig.BALANCE_UPDATE_ROUTING_KEY,
            updateRequest);
        
        logger.info("Sent balance update request with transaction ID: {}", transactionId);
    }
    

    private ExchangeTransaction saveTransaction(Long accountId, String fromCurrency, String toCurrency,
                                              BigDecimal fromAmount, BigDecimal toAmount, String transactionType) {
        ExchangeTransaction transaction = new ExchangeTransaction(
            accountId,
            fromCurrency,
            toCurrency,
            fromAmount,
            toAmount,
            transactionType,
            LocalDateTime.now()
        );
        
        return transactionRepository.save(transaction);
    }

    private ExchangeResponseDto createSuccessResponse(ExchangeRequestDto request, BigDecimal exchangeRate, 
                                                   ConversionResult conversion) {
        ExchangeResponseDto response = new ExchangeResponseDto();
        response.setStatus("SUCCESS");
        response.setMessage(request.getTransactionType().equals("BUY") ? 
                "Purchase completed" : "Sale completed");
        response.setExecutedPrice(exchangeRate);
        response.setTimestamp(LocalDateTime.now());
        response.setFromAmount(conversion.getActualCost());
        response.setFromCurrency(request.getFromCurrency());
        response.setToAmount(conversion.getConvertedAmount());
        response.setToCurrency(request.getToCurrency());
        
        return response;
    }
    

    private ExchangeResponseDto createErrorResponse(String errorMessage) {
        ExchangeResponseDto response = new ExchangeResponseDto();
        response.setStatus("FAILED");
        response.setMessage("Exchange failed: " + errorMessage);
        return response;
    }
    

    private static class ConversionResult {
        private final BigDecimal convertedAmount;
        private final BigDecimal actualCost;
        
        public ConversionResult(BigDecimal convertedAmount, BigDecimal actualCost) {
            this.convertedAmount = convertedAmount;
            this.actualCost = actualCost;
        }
        
        public BigDecimal getConvertedAmount() {
            return convertedAmount;
        }
        
        public BigDecimal getActualCost() {
            return actualCost;
        }
    }
}

