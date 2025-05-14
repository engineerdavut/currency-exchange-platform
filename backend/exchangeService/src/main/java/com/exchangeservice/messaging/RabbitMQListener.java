package com.exchangeservice.messaging;

import com.exchangeservice.config.RabbitMQConfig;
import com.exchangeservice.dto.BalanceCheckResponseDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RabbitMQListener {

    private final Map<String, BalanceCheckResponseDto> balanceResponses = new ConcurrentHashMap<>();
    
    @RabbitListener(queues = RabbitMQConfig.BALANCE_RESPONSE_QUEUE)
    public void receiveBalanceResponse(BalanceCheckResponseDto response) {

        balanceResponses.put(response.getCorrelationId(), response);

        synchronized (response.getCorrelationId().intern()) {
            response.getCorrelationId().intern().notifyAll();
        }
    }
    
    public BalanceCheckResponseDto getBalanceResponse(String correlationId, long timeout) throws InterruptedException {
        if (balanceResponses.containsKey(correlationId)) {
            BalanceCheckResponseDto response = balanceResponses.get(correlationId);
            balanceResponses.remove(correlationId);
            return response;
        }
        
        synchronized (correlationId.intern()) {
            correlationId.intern().wait(timeout);
        }
        
        if (balanceResponses.containsKey(correlationId)) {
            BalanceCheckResponseDto response = balanceResponses.get(correlationId);
            balanceResponses.remove(correlationId);
            return response;
        }
        
        return null; 
    }
}

