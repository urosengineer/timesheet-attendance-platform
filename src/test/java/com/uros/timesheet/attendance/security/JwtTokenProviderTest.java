package com.uros.timesheet.attendance.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    @Mock
    CustomUserDetailsService userDetailsService;

    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String secret = Base64.getEncoder().encodeToString("super-secret-key-for-test-use-123456789012345678".getBytes());
        jwtTokenProvider = new JwtTokenProvider(secret, 60000, 120000, userDetailsService);
    }

    @Test
    void generateToken_and_parseClaims() {
        CustomUserDetails user = mock(CustomUserDetails.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getUsername()).thenReturn("uros");
        when(user.getRoleNames()).thenReturn(Set.of("ADMIN"));
        when(user.getPermissionNames()).thenReturn(Set.of("USER_VIEW", "USER_EDIT"));
        when(user.getOrganizationId()).thenReturn(UUID.randomUUID());
        when(user.getOrganizationName()).thenReturn("DemoOrg");

        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        UUID id = jwtTokenProvider.getUserIdFromJWT(token);
        String username = jwtTokenProvider.getUsernameFromJWT(token);

        assertThat(username).isEqualTo("uros");
        assertThat(id).isEqualTo(user.getId());

        long expiresAt = jwtTokenProvider.getExpirationFromToken(token);
        assertThat(expiresAt).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    void generateRefreshToken_containsTypeRefresh() {
        CustomUserDetails user = mock(CustomUserDetails.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        boolean valid = jwtTokenProvider.validateRefreshToken(refreshToken);
        assertThat(valid).isTrue();
    }
}