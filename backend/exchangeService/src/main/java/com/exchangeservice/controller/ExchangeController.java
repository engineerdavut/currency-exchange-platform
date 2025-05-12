package com.exchangeservice.controller;

import com.exchangeservice.dto.ExchangeRequestDto;
import com.exchangeservice.dto.ExchangeResponseDto;
import com.exchangeservice.service.ExchangeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// java.net.URLDecoder, java.nio.charset.StandardCharsets, java.io.UnsupportedEncodingException
// ve org.springframework.web.server.ResponseStatusException importları buradan kaldırılabilir.

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);
    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    // decodeUsernameFromHeader metodu SİLİNECEK.

    @PostMapping("/process")
    public ResponseEntity<ExchangeResponseDto> processExchange(
            @RequestHeader("X-User") String username, // username artık decode edilmiş gelir
            @RequestBody ExchangeRequestDto request) {
        
        request.setUsername(username); // DTO'ya decode edilmiş username'i set et
        logger.info("Processing exchange request for user: {}", username);
        
        try {
            ExchangeResponseDto responseDto = exchangeService.processExchange(username, request); // Servis metoduna decode edilmiş username'i gönder
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            logger.error("Error processing exchange for user {}: {}", username, e.getMessage(), e);
            ExchangeResponseDto errorResponse = new ExchangeResponseDto();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Exchange processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}