package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.auth.*;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
    void logout(LogoutRequest request);
}
