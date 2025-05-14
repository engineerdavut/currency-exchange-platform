package com.accountservice.integration;

import com.accountservice.dto.LoginRequestDto;
import com.accountservice.dto.LoginResponseDto;
import com.accountservice.dto.RegisterRequestDto;
import com.accountservice.entity.Account;
import com.accountservice.entity.CurrencyType;
import com.accountservice.entity.User;
import com.accountservice.repository.AccountRepository;
import com.accountservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AccountRepository accountRepository;

        @BeforeEach
        public void setup() {
                userRepository.deleteAll();
        }

        @Test
        public void testRegisterAndLogin() throws Exception {
                RegisterRequestDto registerRequest = new RegisterRequestDto("testUser", "password");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User registered successfully"));

                User user = userRepository.findByUsername("testUser").orElse(null);
                assertTrue(user != null);
                assertEquals("testUser", user.getUsername());

                for (CurrencyType currency : CurrencyType.values()) {
                        Account account = accountRepository.findByUserAndCurrencyType(user, currency).orElse(null);
                        assertTrue(account != null);
                        assertEquals(BigDecimal.ZERO, account.getBalance());
                }

                LoginRequestDto loginRequest = new LoginRequestDto("testUser", "password");

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("jwt"))
                                .andReturn();

                String responseContent = result.getResponse().getContentAsString();
                LoginResponseDto loginResponse = objectMapper.readValue(responseContent, LoginResponseDto.class);
                assertEquals("testUser", loginResponse.getUsername());

                mockMvc.perform(get("/api/auth/check")
                                .cookie(result.getResponse().getCookies()))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/auth/logout")
                                .cookie(result.getResponse().getCookies()))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("jwt"))
                                .andExpect(cookie().maxAge("jwt", 0));
        }

        @Test
        public void testRegisterDuplicateUser() throws Exception {
                RegisterRequestDto registerRequest = new RegisterRequestDto("duplicateUser", "password");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        public void testLoginWithInvalidCredentials() throws Exception {
                RegisterRequestDto registerRequest = new RegisterRequestDto("validUser", "password");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                LoginRequestDto loginRequest = new LoginRequestDto("validUser", "wrongPassword");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }
}
