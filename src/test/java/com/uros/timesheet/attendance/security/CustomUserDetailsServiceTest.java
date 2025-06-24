package com.uros.timesheet.attendance.security;

import com.uros.timesheet.attendance.domain.*;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepository;
    @Mock MessageUtil messageUtil;
    @InjectMocks CustomUserDetailsService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private User sampleUser(UUID id, String username) {
        // Minimal valid user (organizacija i role su obavezni za CustomUserDetails)
        Organization org = Organization.builder()
                .id(UUID.randomUUID())
                .name("Org1")
                .timezone("Europe/Belgrade")
                .status("ACTIVE")
                .build();

        Permission perm = Permission.builder()
                .id(UUID.randomUUID())
                .name("USER_VIEW")
                .description("View users")
                .build();

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .permissions(Set.of(perm))
                .build();

        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@test.com")
                .passwordHash("xxx")
                .fullName("Test User")
                .status("ACTIVE")
                .organization(org)
                .roles(Set.of(role))
                .build();
    }

    @Test
    void loadUserByUsername_uuid_ok() {
        UUID userId = UUID.randomUUID();
        User user = sampleUser(userId, "testuser");

        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername(userId.toString());

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_username_ok() {
        User user = sampleUser(UUID.randomUUID(), "anotheruser");

        when(userRepository.findActiveById(any())).thenReturn(Optional.empty());
        when(userRepository.findActiveByUsername("anotheruser")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("anotheruser");

        assertThat(result.getUsername()).isEqualTo("anotheruser");
    }

    @Test
    void loadUserByUsername_uuid_notFound_throws() {
        UUID notFound = UUID.randomUUID();
        when(userRepository.findActiveById(notFound)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername(notFound.toString()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("error.user.not.found");
    }

    @Test
    void loadUserByUsername_username_notFound_throws() {
        when(userRepository.findActiveById(any())).thenReturn(Optional.empty());
        when(userRepository.findActiveByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("error.user.not.found");
    }
}
