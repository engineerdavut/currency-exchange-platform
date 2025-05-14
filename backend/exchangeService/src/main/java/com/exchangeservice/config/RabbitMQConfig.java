package com.exchangeservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BALANCE_CHECK_QUEUE = "balance-check-queue";
    public static final String BALANCE_CHECK_EXCHANGE = "balance-check-exchange";
    public static final String BALANCE_CHECK_ROUTING_KEY = "balance.check";
    
    public static final String BALANCE_UPDATE_QUEUE = "balance-update-queue";
    public static final String BALANCE_UPDATE_EXCHANGE = "balance-update-exchange";
    public static final String BALANCE_UPDATE_ROUTING_KEY = "balance.update";
    
    public static final String BALANCE_RESPONSE_QUEUE = "balance-response-queue";
    public static final String BALANCE_RESPONSE_EXCHANGE = "balance-response-exchange";
    public static final String BALANCE_RESPONSE_ROUTING_KEY = "balance.response";

    @Bean
    public Queue balanceCheckQueue() {
        return new Queue(BALANCE_CHECK_QUEUE, true);
    }

    @Bean
    public DirectExchange balanceCheckExchange() {
        return new DirectExchange(BALANCE_CHECK_EXCHANGE);
    }

    @Bean
    public Binding balanceCheckBinding() {
        return BindingBuilder.bind(balanceCheckQueue())
                .to(balanceCheckExchange())
                .with(BALANCE_CHECK_ROUTING_KEY);
    }
    

    @Bean
    public Queue balanceUpdateQueue() {
        return new Queue(BALANCE_UPDATE_QUEUE, true);
    }

    @Bean
    public DirectExchange balanceUpdateExchange() {
        return new DirectExchange(BALANCE_UPDATE_EXCHANGE);
    }

    @Bean
    public Binding balanceUpdateBinding() {
        return BindingBuilder.bind(balanceUpdateQueue())
                .to(balanceUpdateExchange())
                .with(BALANCE_UPDATE_ROUTING_KEY);
    }
    

    @Bean
    public Queue balanceResponseQueue() {
        return new Queue(BALANCE_RESPONSE_QUEUE, true);
    }

    @Bean
    public DirectExchange balanceResponseExchange() {
        return new DirectExchange(BALANCE_RESPONSE_EXCHANGE);
    }

    @Bean
    public Binding balanceResponseBinding() {
        return BindingBuilder.bind(balanceResponseQueue())
                .to(balanceResponseExchange())
                .with(BALANCE_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}

