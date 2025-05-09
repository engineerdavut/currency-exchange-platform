package com.accountservice.dto;

public class LoginResponseDto {
    private String username;
    
    public LoginResponseDto() {}
    
    public LoginResponseDto(String username) {
        this.username = username;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
