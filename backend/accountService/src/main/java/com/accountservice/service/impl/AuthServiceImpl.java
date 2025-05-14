package com.accountservice.service.impl;

import com.accountservice.dto.*;
import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.User;
import com.accountservice.exception.AuthenticationException;
import com.accountservice.repository.UserRepository;
import com.accountservice.security.JwtTokenProvider;
import com.accountservice.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import com.accountservice.exception.RegistrationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;

    public AuthServiceImpl(UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountRepository = accountRepository;
    }

    @Override
    public String authenticate(LoginRequestDto loginRequest) {
        logger.debug("[AuthService] authenticate() username={}", loginRequest.getUsername());
        var userOpt = userRepository.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            logger.warn("[AuthService] authenticate: user not found {}", loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            logger.warn("[AuthService] authenticate: invalid password for {}", loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }
        return user.getUsername();
    }

    @Override
    public LoginResponseDto loginAndSetCookies(LoginRequestDto loginRequest,
            HttpServletResponse response) {
        logger.debug("[AuthService] loginAndSetCookies() request={}", loginRequest);
        String username = authenticate(loginRequest);

        boolean prod = false;
        String token = jwtTokenProvider.generateToken(username);
        logger.debug("[AuthService] loginAndSetCookies: generated token for cookie: {}",
                token.substring(0, 15) + "...");
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                .path("/")
                .httpOnly(true)
                .secure(prod)
                .sameSite(prod ? "None" : "Lax")
                .maxAge(Duration.ofHours(1))
                .build();
        logger.debug("[AuthService] loginAndSetCookies: setting cookie {}", jwtCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        return new LoginResponseDto(username);

    }

    @Override
    @Transactional
    public void register(RegisterRequestDto registerRequest) {
        logger.debug("[AuthService] register() username={}", registerRequest.getUsername());

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            logger.warn("[AuthService] register: user already exists {}", registerRequest.getUsername());
            throw new RegistrationException("Username '" + registerRequest.getUsername() + "' is already taken.");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(registerRequest.getPassword());
        logger.info("[AuthService] register: Creating new user {}", newUser.getUsername());

        User savedUser = userRepository.save(newUser);
        logger.info("[AuthService] register: User saved successfully with ID: {}", savedUser.getId());

        for (CurrencyType currency : CurrencyType.values()) {
            createInitialAccount(savedUser, currency);
        }

        logger.info("[AuthService] register: Initial accounts created for user {}", savedUser.getUsername());
    }

    private void createInitialAccount(User user, CurrencyType currency) {
        Account account = new Account();
        account.setUser(user);
        account.setCurrencyType(currency);
        account.setBalance(BigDecimal.ZERO);
        accountRepository.save(account);
        logger.debug("[AuthService] createInitialAccount: Created {} account for user {}", currency,
                user.getUsername());
    }

    @Override
    public void logout(HttpServletResponse response) {
        logger.debug("[AuthService] logout()");
        ResponseCookie deleteJwt = ResponseCookie.from("jwt", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        logger.debug("[AuthService] logout: clearing cookie {}", deleteJwt);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteJwt.toString());
    }

    @Override
    public boolean validateAuthToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        logger.debug("[AuthService] validateAuthToken: cookies={}",
                cookies == null ? "null" : Arrays.toString(cookies));
        if (cookies == null)
            return false;
        for (Cookie c : cookies) {
            logger.debug("[AuthService] validateAuthToken: cookie name={} value={}", c.getName(), c.getValue());
            if ("jwt".equals(c.getName())) {
                boolean valid = jwtTokenProvider.validateToken(c.getValue());
                logger.debug("[AuthService] validateAuthToken: token valid={}", valid);
                return valid;
            }
        }
        logger.debug("[AuthService] validateAuthToken: jwt cookie not found");
        return false;
    }
}
