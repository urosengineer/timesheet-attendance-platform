package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.auth.*;
import com.uros.timesheet.attendance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController handles authentication endpoints, such as user login, refresh, and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication and JWT token management"
)
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     */
    @Operation(summary = "User login", description = "Authenticates the user and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody(description = "Authentication credentials", required = true, content = @Content(schema = @Schema(implementation = AuthRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Refreshes the JWT token using a valid refresh token.
     */
    @Operation(summary = "Refresh access token", description = "Returns a new access token using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed", content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @RequestBody(description = "Refresh token payload", required = true, content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Logs out the user by blacklisting the refresh token.
     */
    @Operation(summary = "Logout", description = "Invalidates the refresh token (logout).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(description = "Logout request", required = true, content = @Content(schema = @Schema(implementation = LogoutRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}