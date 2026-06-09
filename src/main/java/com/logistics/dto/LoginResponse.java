package com.logistics.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String realName;
    private String role;
}
