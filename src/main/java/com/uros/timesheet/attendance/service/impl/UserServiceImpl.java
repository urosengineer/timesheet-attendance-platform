package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.domain.Team;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.user.UserCreateRequest;
import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.UserMapper;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.RoleRepository;
import com.uros.timesheet.attendance.repository.TeamRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.UserService;
import com.uros.timesheet.attendance.service.helper.UserAuditLogHelper;
import com.uros.timesheet.attendance.service.helper.UserMetricHelper;
import com.uros.timesheet.attendance.service.helper.UserNotificationHelper;
import com.uros.timesheet.attendance.service.helper.UserValidationService;
import com.uros.timesheet.attendance.util.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.uros.timesheet.attendance.exception.NotFoundException;

import io.micrometer.core.instrument.Timer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final UserMapper userMapper;
    private final MessageUtil messageUtil;

    private final UserValidationService userValidationService;
    private final UserAuditLogHelper userAuditLogHelper;
    private final UserNotificationHelper userNotificationHelper;
    private final UserMetricHelper userMetricHelper;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        Timer.Sample sample = userMetricHelper.startCreateUserTimer();
        boolean success = false;
        try {
            userValidationService.validateCreateRequest(request);
            Organization organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new NotFoundException(messageUtil.get("error.organization.not.found")));
            Set<Role> roles = new HashSet<>();
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                for (UUID roleId : request.getRoleIds()) {
                    Role role = roleRepository.findById(roleId)
                            .orElseThrow(() -> new NotFoundException(messageUtil.get("error.role.not.found", roleId)));
                    roles.add(role);
                }
            }
            Team team = null;
            if (request.getTeamId() != null) {
                team = teamRepository.findById(request.getTeamId())
                        .orElseThrow(() -> new NotFoundException(messageUtil.get("error.team.not.found")));
            }
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(userValidationService.hashPassword(request.getPassword()))
                    .fullName(request.getFullName())
                    .status("ACTIVE")
                    .organization(organization)
                    .roles(roles)
                    .team(team)
                    .build();
            userRepository.save(user);
            userAuditLogHelper.logCreateUser(user);
            success = true;
            return userMapper.toResponse(user);
        } finally {
            userMetricHelper.stopCreateUserTimer(sample, success);
        }
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException(messageUtil.get("error.user.not.found")));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse softDeleteUser(UUID id, UUID performedByUserId, String reason) {
        Timer.Sample sample = userMetricHelper.startSoftDeleteUserTimer();
        boolean success = false;
        try {
            User user = userRepository.findActiveById(id)
                    .orElseThrow(() -> new NotFoundException(messageUtil.get("error.user.not.found")));
            userValidationService.ensureNotDeleted(user);

            user.markDeleted();
            userRepository.save(user);

            userAuditLogHelper.logSoftDeleteUser(user, performedByUserId, reason);
            userNotificationHelper.sendSoftDeleteNotification(user, reason);

            success = true;
            return userMapper.toResponse(user);
        } finally {
            userMetricHelper.stopSoftDeleteUserTimer(sample, success);
        }
    }

    @Override
    @Transactional
    public UserResponse restoreUser(UUID id, UUID performedByUserId, String reason) {
        Timer.Sample sample = userMetricHelper.startRestoreUserTimer();
        boolean success = false;
        try {
            User user = userRepository.findByIdIncludingDeleted(id)
                    .orElseThrow(() -> new NotFoundException(messageUtil.get("error.user.not.found")));
            userValidationService.ensureDeleted(user);

            user.restore();
            userRepository.save(user);

            userAuditLogHelper.logRestoreUser(user, performedByUserId, reason);
            userNotificationHelper.sendRestoreNotification(user, reason);

            success = true;
            return userMapper.toResponse(user);
        } finally {
            userMetricHelper.stopRestoreUserTimer(sample, success);
        }
    }

    @Override
    public List<UserResponse> getUsersForCurrentTenant() {
        String tenantIdString = TenantContext.getTenantId();
        if (tenantIdString == null) {
            throw new IllegalStateException(messageUtil.get("error.tenant.not.set"));
        }
        UUID tenantId = UUID.fromString(tenantIdString);
        List<User> users = userRepository.findByOrganizationId(tenantId);
        return users.stream().map(userMapper::toResponse).toList();
    }
}