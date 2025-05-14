package com.apigateway.filter;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtCookieToHeaderFilterTest {

    @Mock
    private JwtTokenValidator jwtValidator;

    @Mock
    private GatewayFilterChain filterChain; 

    @InjectMocks
    private JwtCookieToHeaderFilter jwtCookieToHeaderFilter;

    @Captor
    private ArgumentCaptor<ServerWebExchange> exchangeCaptor;


    @Test
    void filter_WhenPathIsAuthPath_ShouldSkipValidationAndChain() {

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());


        Mono<Void> result = jwtCookieToHeaderFilter.filter(exchange, filterChain);


        StepVerifier.create(result).verifyComplete();
        verify(jwtValidator, never()).validateAndExtractUser(any());
        verify(filterChain).filter(exchange);
        assertNull(exchange.getRequest().getHeaders().getFirst("X-User"));
    }

    @Test
    void filter_WhenValidTokenAndProtectedRoute_ShouldValidateAddHeaderAndChain() {

        String username = "testUser";
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/exchange/process")
                .cookie(new HttpCookie("jwt", "valid.token"))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(jwtValidator.validateAndExtractUser(request)).thenReturn(username);

        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());



        Mono<Void> result = jwtCookieToHeaderFilter.filter(exchange, filterChain);


        StepVerifier.create(result).verifyComplete();
        verify(jwtValidator).validateAndExtractUser(request);
        verify(filterChain).filter(exchangeCaptor.capture());
        assertEquals(username, exchangeCaptor.getValue().getRequest().getHeaders().getFirst("X-User"));
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_WhenNoTokenAndProtectedRoute_ShouldReturnUnauthorized() {

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/account/wallet").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(jwtValidator.validateAndExtractUser(request)).thenReturn(null);


        Mono<Void> result = jwtCookieToHeaderFilter.filter(exchange, filterChain);


        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtValidator).validateAndExtractUser(request);
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_WhenNoTokenAndPublicPath_ShouldChainWithoutHeader() {

        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(jwtValidator.validateAndExtractUser(request)).thenReturn(null);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());


        Mono<Void> result = jwtCookieToHeaderFilter.filter(exchange, filterChain);


        StepVerifier.create(result).verifyComplete();
        verify(jwtValidator).validateAndExtractUser(request);
        verify(filterChain).filter(exchange);
        assertNull(exchange.getRequest().getHeaders().getFirst("X-User"));
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_WhenExpiredTokenAndProtectedRoute_ShouldReturnUnauthorized() {

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/account/wallet")
                .cookie(new HttpCookie("jwt", "expired.token"))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtValidator.validateAndExtractUser(request)).thenReturn(null);



        Mono<Void> result = jwtCookieToHeaderFilter.filter(exchange, filterChain);


        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtValidator).validateAndExtractUser(request);
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_WhenInvalidTokenAndProtectedRoute_ShouldReturnUnauthorized() {

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/account/wallet")
                .cookie(new HttpCookie("jwt", "invalid.token"))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(jwtValidator.validateAndExtractUser(request)).thenReturn(null);



        Mono<Void> result = jwtCookieToHeaderFilter.filter(exchange, filterChain);


        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtValidator).validateAndExtractUser(request);
        verify(filterChain, never()).filter(any());
    }
}