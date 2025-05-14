package com.accountservice.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Profile("dev")
public class CorsTestController {

    @GetMapping("/cors")
    public Map<String, String> testCors() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS is working!");
        return response;
    }
}