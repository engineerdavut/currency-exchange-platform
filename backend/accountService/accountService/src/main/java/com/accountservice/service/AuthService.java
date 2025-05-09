package com.accountservice.service;

import com.accountservice.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponseDto loginAndSetCookies(LoginRequestDto loginRequest, HttpServletResponse response);
    void register(RegisterRequestDto registerRequest);
    void logout(HttpServletResponse response);
    boolean validateAuthToken(HttpServletRequest request);
    String authenticate(LoginRequestDto loginRequest);
}