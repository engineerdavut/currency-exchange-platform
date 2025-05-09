package com.accountservice.controller;

import com.accountservice.dto.LoginRequestDto;
import com.accountservice.dto.LoginResponseDto;
import com.accountservice.dto.RegisterRequestDto;
import com.accountservice.exception.AuthenticationException;
import com.accountservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testLoginSuccess() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto("testUser", "password");
        // Düzeltme: Beklenen DTO'yu username ile oluştur
        LoginResponseDto expectedResponse = new LoginResponseDto("testUser");
        when(authService.loginAndSetCookies(loginRequest, response)).thenReturn(expectedResponse);
    
        // Act
        ResponseEntity<LoginResponseDto> result = authController.login(loginRequest, response);
    
        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse.getUsername(), result.getBody().getUsername()); // İçeriği kontrol et
        verify(authService).loginAndSetCookies(loginRequest, response);
    }

    @Test
    public void testLoginFailure() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto("testUser", "wrongPassword");
        // Düzeltme: Belirli bir AuthenticationException fırlatıldığını varsayalım
        when(authService.loginAndSetCookies(loginRequest, response))
            .thenThrow(new AuthenticationException("Invalid credentials")); // Gerçek exception tipi
   
        // Act & Assert - Hatanın controller'dan fırlatılmasını bekleyebiliriz
        // VEYA eğer bir @ExceptionHandler varsa onun döndüğü ResponseEntity'yi test ederiz.
        // Şimdilik, bir ExceptionHandler'ın 401 döndürdüğünü varsayarak basit bir kontrol:
        try {
           authController.login(loginRequest, response);
           // Eğer buraya gelirse test fail etmeli, çünkü exception bekleniyordu.
           org.junit.jupiter.api.Assertions.fail("Expected AuthenticationException to be thrown");
        } catch (AuthenticationException e) {
           // Exception beklendiği gibi fırlatıldı. Test başarılı.
           // VEYA eğer controller advice 401 dönüyorsa:
           // ResponseEntity<LoginResponseDto> result = authController.login(loginRequest, response);
           // assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        }
   
        verify(authService).loginAndSetCookies(loginRequest, response); // Servis çağrısını yine de doğrula
    }

    @Test
    public void testRegisterSuccess() {
        // Arrange
        RegisterRequestDto registerRequest = new RegisterRequestDto("newUser", "password");
        doNothing().when(authService).register(registerRequest);

        // Act
        ResponseEntity<String> result = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("User registered successfully"));
        verify(authService).register(registerRequest);
    }

    @Test
    public void testLogout() {
        // Act
        ResponseEntity<String> result = authController.logout(response);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Logged out successfully"));
        verify(authService).logout(response);
    }

    @Test
    public void testCheckAuthSuccess() {
        // Arrange
        when(authService.validateAuthToken(request)).thenReturn(true);

        // Act
        ResponseEntity<?> result = authController.checkAuth(request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(authService).validateAuthToken(request);
    }

    @Test
    public void testCheckAuthFailure() {
        // Arrange
        when(authService.validateAuthToken(request)).thenReturn(false);

        // Act
        ResponseEntity<?> result = authController.checkAuth(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        verify(authService).validateAuthToken(request);
    }
}
