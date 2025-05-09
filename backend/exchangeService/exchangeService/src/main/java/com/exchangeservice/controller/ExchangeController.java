package com.exchangeservice.controller; // Paket adını kontrol edin

import com.exchangeservice.dto.ExchangeRequestDto;
import com.exchangeservice.dto.ExchangeResponseDto;
import com.exchangeservice.service.ExchangeService;

import org.springframework.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    private final ExchangeService exchangeService;

    @Autowired
    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/process")
    public ResponseEntity<ExchangeResponseDto> processExchange(
            @RequestHeader("X-User") String username,
            @RequestBody ExchangeRequestDto request) {
        try {
            logger.info("Processing exchange request for user: {}", username);
            request.setUsername(username);
            
            ExchangeResponseDto responseDto = exchangeService.processExchange(username, request);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            logger.error("Error processing exchange: {}", e.getMessage(), e);
            ExchangeResponseDto errorResponse = new ExchangeResponseDto();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Exchange processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}