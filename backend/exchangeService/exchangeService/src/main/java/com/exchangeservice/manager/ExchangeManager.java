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

    // Bu metod, hesap servisi ile entegrasyon (REST çağrısı vs.) sonrası işlemi gerçekleştirir.
    @Transactional
    public ExchangeResponseDto processExchange(String username,ExchangeRequestDto request) throws Exception {
        // Örneğin, hesap servisine REST çağrısı yaparak bakiyeyi doğrulayabilirsiniz.
        // RestTemplate veya WebClient kullanarak Account Service'in /wallet veya /info endpoint'ini çağırın.
        // Eğer bakiye yeterliyse, işlem yapılmasına izin verin; değilse hata fırlatın.
        // (Bu örnekte, bu kısım demo amaçlı atlanmıştır.)
        
        return exchangeService.processExchange(username,request);
    }
}
