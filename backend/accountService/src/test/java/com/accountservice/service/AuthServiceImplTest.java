package com.accountservice.service;

import com.accountservice.dto.LoginRequestDto;
import com.accountservice.dto.LoginResponseDto;
import com.accountservice.dto.RegisterRequestDto;
import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.User;
import com.accountservice.exception.AuthenticationException;
import com.accountservice.repository.AccountRepository;
import com.accountservice.repository.UserRepository;
import com.accountservice.security.JwtTokenProvider;
import com.accountservice.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.Duration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequestDto validLoginRequest;
    private RegisterRequestDto validRegisterRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "password");
        validLoginRequest = new LoginRequestDto("testUser", "password");
        validRegisterRequest = new RegisterRequestDto("newUser", "password");
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnUsername() {
        when(userRepository.findByUsername("testUser"))
            .thenReturn(Optional.of(testUser));

        String username = authService.authenticate(validLoginRequest);

        assertEquals("testUser",username);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void authenticate_WithInvalidUsername_ShouldThrowException() {
        when(userRepository.findByUsername("testUser"))
            .thenReturn(Optional.empty());

        AuthenticationException ex = assertThrows(
            AuthenticationException.class,
            () -> authService.authenticate(validLoginRequest)
        );
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowException() {
        when(userRepository.findByUsername("testUser"))
            .thenReturn(Optional.of(testUser));

        LoginRequestDto badPass = new LoginRequestDto("testUser", "wrong");
        AuthenticationException ex = assertThrows(
            AuthenticationException.class,
            () -> authService.authenticate(badPass)
        );
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void loginAndSetCookies_ShouldAddJwtCookieAndReturnDto() {
        when(userRepository.findByUsername("testUser"))
            .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken("testUser"))
            .thenReturn("valid.jwt.token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginResponseDto dto = authService.loginAndSetCookies(validLoginRequest, response);

        assertNotNull(dto);
        assertEquals("testUser", dto.getUsername());

        Cookie jwtCookie = response.getCookie("jwt");
        assertNotNull(jwtCookie);
        assertEquals("valid.jwt.token", jwtCookie.getValue());
        assertTrue(jwtCookie.isHttpOnly());
        assertEquals("/", jwtCookie.getPath());
        // maxAge birim saniye: Duration.ofHours(1) → 3600 sn
        assertEquals(Duration.ofHours(1).getSeconds(), jwtCookie.getMaxAge());

        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void register_WithNewUser_ShouldSaveUserAndAccounts() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        authService.register(validRegisterRequest);

        verify(userRepository).findByUsername("newUser");
        verify(userRepository).save(argThat(u -> 
            u.getUsername().equals("newUser") && u.getPassword().equals("password")
        ));
        // Hesap kaydı CurrencyType kadar çağrılmış olmalı
        verify(accountRepository, times(CurrencyType.values().length))
            .save(any(Account.class));
    }

    @Test
    void register_WithExistingUser_ShouldThrow() {
        when(userRepository.findByUsername("newUser"))
            .thenReturn(Optional.of(new User("newUser","x")));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> authService.register(validRegisterRequest)
        );
        assertEquals("Username '" + validRegisterRequest.getUsername() + "' is already taken.", ex.getMessage());
    }

    @Test
    void logout_ShouldClearJwtCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        authService.logout(response);

        Cookie jwtCookie = response.getCookie("jwt");
        assertNotNull(jwtCookie);
        assertEquals("", jwtCookie.getValue());
        assertEquals("/", jwtCookie.getPath());
        assertEquals(0, jwtCookie.getMaxAge());
    }

    @Test
    void validateAuthToken_WithValidToken_ShouldReturnTrue() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("jwt","valid.token"));
        when(jwtTokenProvider.validateToken("valid.token")).thenReturn(true);

        boolean ok = authService.validateAuthToken(req);
        assertTrue(ok);
        verify(jwtTokenProvider).validateToken("valid.token");
    }

    @Test
    void validateAuthToken_WithInvalidToken_ShouldReturnFalse() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("jwt","bad.token"));
        when(jwtTokenProvider.validateToken("bad.token")).thenReturn(false);

        boolean ok = authService.validateAuthToken(req);
        assertFalse(ok);
        verify(jwtTokenProvider).validateToken("bad.token");
    }

    @Test
    void validateAuthToken_WithNoCookies_ShouldReturnFalse() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        boolean ok = authService.validateAuthToken(req);
        assertFalse(ok);
        verifyNoInteractions(jwtTokenProvider);
    }
}
