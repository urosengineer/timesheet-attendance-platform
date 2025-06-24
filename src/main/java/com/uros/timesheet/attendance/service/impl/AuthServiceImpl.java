package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.dto.auth.*;
import com.uros.timesheet.attendance.security.CustomUserDetails;
import com.uros.timesheet.attendance.security.JwtTokenProvider;
import com.uros.timesheet.attendance.service.AuthService;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MessageUtil messageUtil;

    private final Set<String> blacklistedRefreshTokens = ConcurrentHashMap.newKeySet();

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        long expiresAt = jwtTokenProvider.getExpirationFromToken(token);

        AuthResponse response = new AuthResponse();
        response.setId(userDetails.getId());
        response.setUsername(userDetails.getUsername());
        response.setFullName(userDetails.getFullName());
        response.setAccessToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresAt(expiresAt);
        response.setOrganizationId(userDetails.getOrganizationId());
        response.setOrganizationName(userDetails.getOrganizationName());
        response.setRoles(List.copyOf(userDetails.getRoleNames()));
        response.setPermissions(List.copyOf(userDetails.getPermissionNames()));
        return response;
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || blacklistedRefreshTokens.contains(request.getRefreshToken())) {
            throw new IllegalArgumentException(messageUtil.get("error.auth.invalid_refresh_token"));
        }
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new IllegalArgumentException(messageUtil.get("error.auth.expired_refresh_token"));
        }

        CustomUserDetails userDetails = jwtTokenProvider.getUserDetailsFromRefreshToken(request.getRefreshToken());

        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        blacklistedRefreshTokens.add(request.getRefreshToken());

        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresAt(jwtTokenProvider.getExpirationFromToken(newAccessToken));
        return response;
    }

    @Override
    public void logout(LogoutRequest request) {
        if (request.getRefreshToken() != null) {
            blacklistedRefreshTokens.add(request.getRefreshToken());
        }
    }
}