package com.example.memberservice.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
