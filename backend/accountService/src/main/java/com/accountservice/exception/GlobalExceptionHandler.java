// src/main/java/com/accountservice/exception/GlobalExceptionHandler.java
package com.accountservice.exception;

import com.accountservice.dto.ErrorResponseDto; // Bu DTO'yu oluşturduğunuzdan emin olun
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorResponseDto> handleRegistrationException(RegistrationException ex, WebRequest request) {
        logger.warn("Registration failed: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto("REGISTRATION_ERROR", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex,
            WebRequest request) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto("AUTHENTICATION_ERROR", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex,
            WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto("RESOURCE_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientBalanceException(InsufficientBalanceException ex,
            WebRequest request) {
        logger.warn("Insufficient balance: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto("INSUFFICIENT_BALANCE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex,
            WebRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto("ILLEGAL_ARGUMENT", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: ", ex);
        ErrorResponseDto errorResponse = new ErrorResponseDto("INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please try again later.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}