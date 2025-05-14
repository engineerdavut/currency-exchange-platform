package com.exchangeservice.manager;

import com.exchangeservice.dto.ExchangeRequestDto;
import com.exchangeservice.dto.ExchangeResponseDto;
import com.exchangeservice.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExchangeManager {

    @Autowired
    private ExchangeService exchangeService;

    @Transactional
    public ExchangeResponseDto processExchange(String username,ExchangeRequestDto request) throws Exception {
        
        return exchangeService.processExchange(username,request);
    }
}
