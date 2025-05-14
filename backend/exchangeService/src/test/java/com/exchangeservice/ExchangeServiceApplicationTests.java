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
    @MockitoBean(name = "APILayerManager") 
    private PriceManager apiLayerManagerMock;

    @MockitoBean(name = "exchangeRateAPIManager") 
    private PriceManager exchangeRateAPIManagerMock;

    @MockitoBean
    private RabbitTemplate rabbitTemplateMock;

    @MockitoBean
    private RabbitMQListener rabbitMQListenerMock;

	@Test
	void contextLoads() {
	}

}
