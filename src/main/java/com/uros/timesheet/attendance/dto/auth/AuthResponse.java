package com.uros.timesheet.attendance.dto.auth;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AuthResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresAt;
    private UUID organizationId;
    private String organizationName;
    private List<String> roles;
    private List<String> permissions;
}