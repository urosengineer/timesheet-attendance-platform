package com.uros.timesheet.attendance.security;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.uros.timesheet.attendance.exception.NotFoundException;

import java.util.UUID;

/**
 * Custom UserDetailsService implementation supporting both username and UUID lookup.
 * Enables authentication and principal resolution by both username (for login)
 * and userId (for JWT and WebSocket authentication).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    /**
     * Loads a user by username or UUID (user id).
     * If input is a valid UUID, tries to find by id.
     * Otherwise, tries to find by username.
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        try {
            UUID id = UUID.fromString(usernameOrId);
            User user = userRepository.findActiveById(id)
                    .orElseThrow(() -> new NotFoundException("error.user.not.found"));
            return new CustomUserDetails(user);
        } catch (IllegalArgumentException ex) {
            User user = userRepository.findActiveByUsername(usernameOrId)
                    .orElseThrow(() -> new NotFoundException("error.user.not.found"));
            return new CustomUserDetails(user);
        }
    }
}