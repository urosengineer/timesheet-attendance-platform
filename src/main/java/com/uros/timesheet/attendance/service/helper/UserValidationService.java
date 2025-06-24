package com.uros.timesheet.attendance.service.helper;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.user.UserCreateRequest;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UserValidationService {

    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    public void validateCreateRequest(UserCreateRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException(messageUtil.get("error.user.username.required"));
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException(messageUtil.get("error.user.email.required"));
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException(messageUtil.get("error.user.password.required"));
        }
        if (request.getOrganizationId() == null) {
            throw new IllegalArgumentException(messageUtil.get("error.organization.id.required"));
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException(messageUtil.get("error.user.username.exists"));
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException(messageUtil.get("error.user.email.exists"));
        }
    }

    public void ensureNotDeleted(User user) {
        if (user.isDeleted()) {
            throw new IllegalStateException(messageUtil.get("error.user.already.deleted"));
        }
    }

    public void ensureDeleted(User user) {
        if (!user.isDeleted()) {
            throw new IllegalStateException(messageUtil.get("error.user.not.deleted"));
        }
    }

    public String hashPassword(String password) {
        // For demo only — in production, use BCrypt or Argon2!
        return "{noop}" + password;
    }
}