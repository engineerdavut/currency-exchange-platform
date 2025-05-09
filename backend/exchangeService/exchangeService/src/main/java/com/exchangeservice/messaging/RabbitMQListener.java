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
        // Correlation ID ile cevabı saklayalım
        balanceResponses.put(response.getCorrelationId(), response);
        
        // Senkronizasyon için notify
        synchronized (response.getCorrelationId().intern()) {
            response.getCorrelationId().intern().notifyAll();
        }
    }
    
    public BalanceCheckResponseDto getBalanceResponse(String correlationId, long timeout) throws InterruptedException {
        // Cevap zaten geldiyse hemen döndür
        if (balanceResponses.containsKey(correlationId)) {
            BalanceCheckResponseDto response = balanceResponses.get(correlationId);
            balanceResponses.remove(correlationId);
            return response;
        }
        
        // Cevap gelmemişse bekle
        synchronized (correlationId.intern()) {
            correlationId.intern().wait(timeout);
        }
        
        // Timeout sonrası kontrol et
        if (balanceResponses.containsKey(correlationId)) {
            BalanceCheckResponseDto response = balanceResponses.get(correlationId);
            balanceResponses.remove(correlationId);
            return response;
        }
        
        return null; // Timeout
    }
}

