package com.accountservice.messaging;

import com.accountservice.config.RabbitMQConfig;
import com.accountservice.dto.BalanceCheckRequestDto;
import com.accountservice.dto.BalanceCheckResponseDto;
import com.accountservice.dto.BalanceUpdateRequestDto;
import com.accountservice.dto.ExchangeTransactionDto;
import com.accountservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final AccountService accountService;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQListener(AccountService accountService, RabbitTemplate rabbitTemplate) {
        this.accountService = accountService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.BALANCE_CHECK_QUEUE)
    public void checkBalance(BalanceCheckRequestDto request) {
        logger.info("Received balance check request for user: {}, currency: {}, amount: {}",
                request.getUsername(), request.getCurrency(), request.getAmount());

        boolean hasEnoughBalance = false;

        try {
            hasEnoughBalance = accountService.hasEnoughBalance(
                    request.getUsername(),
                    request.getCurrency(),
                    request.getAmount());

            logger.info("Balance check result for user {}: {}",
                    request.getUsername(), hasEnoughBalance ? "Sufficient" : "Insufficient");
        } catch (Exception e) {
            logger.error("Error checking balance: {}", e.getMessage(), e);
        }

        BalanceCheckResponseDto response = new BalanceCheckResponseDto();
        response.setHasEnoughBalance(hasEnoughBalance);
        response.setCorrelationId(request.getCorrelationId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BALANCE_RESPONSE_EXCHANGE,
                RabbitMQConfig.BALANCE_RESPONSE_ROUTING_KEY,
                response);

        logger.info("Sent balance check response for correlation ID: {}", request.getCorrelationId());
    }

    @RabbitListener(queues = RabbitMQConfig.BALANCE_UPDATE_QUEUE)
    public void updateBalance(BalanceUpdateRequestDto request) {
        logger.info("Received balance update request for user: {}, transaction ID: {}",
                request.getUsername(), request.getTransactionId());

        try {
            ExchangeTransactionDto exchangeRequest = new ExchangeTransactionDto(
                    request.getUsername(),
                    request.getFromCurrency(),
                    request.getToCurrency(),
                    request.getFromAmount(),
                    request.getToAmount());

            accountService.exchangeCurrency(exchangeRequest);

            logger.info("Exchange completed successfully for transaction ID: {}", request.getTransactionId());
        } catch (Exception e) {
            logger.error("Error processing exchange: {}", e.getMessage(), e);
        }
    }
}
