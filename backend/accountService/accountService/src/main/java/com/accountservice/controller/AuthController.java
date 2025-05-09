package com.accountservice.controller;

import com.accountservice.dto.*;
import com.accountservice.service.AuthService;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto,
                                                  HttpServletResponse resp) {
        var loginResponse = authService.loginAndSetCookies(dto, resp);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto dto) {
        authService.register(dto);
        return ResponseEntity.ok("{\"message\":\"User registered successfully\"}");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse resp) {
        authService.logout(resp);
        return ResponseEntity.ok("{\"message\":\"Logged out successfully\"}");
    }

    @GetMapping("/check")
    public ResponseEntity<Void> checkAuth(HttpServletRequest req) {
        return authService.validateAuthToken(req)
            ? ResponseEntity.ok().build()
            : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
