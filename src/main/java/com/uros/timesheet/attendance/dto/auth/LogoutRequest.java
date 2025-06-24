package com.uros.timesheet.attendance.dto.auth;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}