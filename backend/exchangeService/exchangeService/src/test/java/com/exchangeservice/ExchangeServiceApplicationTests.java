package com.exchangeservice;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.exchangeservice.manager.PriceManager;
import com.exchangeservice.messaging.RabbitMQListener;

@SpringBootTest
@ActiveProfiles("test")
class ExchangeServiceApplicationTests {
    @MockitoBean(name = "APILayerManager") // veya @Qualifier ile aynı isim
    private PriceManager apiLayerManagerMock;

    // Eğer ExchangeService @Qualifier("exchangeRateAPIManager") PriceManager alıyorsa:
    @MockitoBean(name = "exchangeRateAPIManager") // veya @Qualifier ile aynı isim
    private PriceManager exchangeRateAPIManagerMock;

    // Eğer CollectApiPriceManager da bir bean ise ve sorun çıkarıyorsa
    // (önceki çözümde bu sorunu çözdüğümüzü varsayıyorum,
    // yani CollectApiPriceManager'daki @Value'lar artık ${api.collectapi.key} gibi):
    // @MockBean(name = "collectApiPriceManager")
    // private PriceManager collectApiPriceManagerMock; // Veya CollectApiPriceManager tipiyle

    // ExchangeService'in diğer bağımlılıkları (RabbitMQ vs.) varsa onlar da mock'lanabilir
    @MockitoBean
    private RabbitTemplate rabbitTemplateMock;

    @MockitoBean
    private RabbitMQListener rabbitMQListenerMock;

	@Test
	void contextLoads() {
	}

}
