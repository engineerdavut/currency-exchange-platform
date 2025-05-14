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

        LoginRequestDto loginRequest = new LoginRequestDto("testUser", "password");

        LoginResponseDto expectedResponse = new LoginResponseDto("testUser");
        when(authService.loginAndSetCookies(loginRequest, response)).thenReturn(expectedResponse);

        ResponseEntity<LoginResponseDto> result = authController.login(loginRequest, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse.getUsername(), result.getBody().getUsername());
        verify(authService).loginAndSetCookies(loginRequest, response);
    }

    @Test
    public void testLoginFailure() {

        LoginRequestDto loginRequest = new LoginRequestDto("testUser", "wrongPassword");
        when(authService.loginAndSetCookies(loginRequest, response))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        try {
            authController.login(loginRequest, response);
            org.junit.jupiter.api.Assertions.fail("Expected AuthenticationException to be thrown");
        } catch (AuthenticationException e) {

        }

        verify(authService).loginAndSetCookies(loginRequest, response);
    }

    @Test
    public void testRegisterSuccess() {

        RegisterRequestDto registerRequest = new RegisterRequestDto("newUser", "password");
        doNothing().when(authService).register(registerRequest);

        ResponseEntity<String> result = authController.register(registerRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("User registered successfully"));
        verify(authService).register(registerRequest);
    }

    @Test
    public void testLogout() {

        ResponseEntity<String> result = authController.logout(response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Logged out successfully"));
        verify(authService).logout(response);
    }

    @Test
    public void testCheckAuthSuccess() {

        when(authService.validateAuthToken(request)).thenReturn(true);

        ResponseEntity<?> result = authController.checkAuth(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(authService).validateAuthToken(request);
    }

    @Test
    public void testCheckAuthFailure() {

        when(authService.validateAuthToken(request)).thenReturn(false);

        ResponseEntity<?> result = authController.checkAuth(request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        verify(authService).validateAuthToken(request);
    }
}
