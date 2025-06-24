package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.domain.Team;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.user.UserCreateRequest;
import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.UserMapper;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.RoleRepository;
import com.uros.timesheet.attendance.repository.TeamRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.helper.UserAuditLogHelper;
import com.uros.timesheet.attendance.service.helper.UserMetricHelper;
import com.uros.timesheet.attendance.service.helper.UserNotificationHelper;
import com.uros.timesheet.attendance.service.helper.UserValidationService;
import com.uros.timesheet.attendance.util.TenantContext;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private UserMapper userMapper;
    @Mock private MessageUtil messageUtil;
    @Mock private UserValidationService userValidationService;
    @Mock private UserAuditLogHelper userAuditLogHelper;
    @Mock private UserNotificationHelper userNotificationHelper;
    @Mock private UserMetricHelper userMetricHelper;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void createUser_success() {
        // Arrange
        UUID orgId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("secret");
        request.setFullName("Test User");
        request.setOrganizationId(orgId);
        request.setTeamId(teamId);
        request.setRoleIds(Set.of(roleId));

        Organization organization = Organization.builder().id(orgId).name("Org1").build();
        Team team = Team.builder().id(teamId).name("A Team").organization(organization).build();
        Role role = Role.builder().id(roleId).name("ADMIN").build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userValidationService.hashPassword("secret")).thenReturn("hashedPassword");

        User userToSave = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .status("ACTIVE")
                .organization(organization)
                .team(team)
                .roles(Set.of(role))
                .build();

        User savedUser = User.builder().id(UUID.randomUUID()).username("testuser").organization(organization).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("testuser");
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // Mock for Timer.Sample
        Timer.Sample sample = mock(Timer.Sample.class);
        when(userMetricHelper.startCreateUserTimer()).thenReturn(sample);

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userValidationService).validateCreateRequest(request);
        verify(userRepository).save(any(User.class));
        verify(userAuditLogHelper).logCreateUser(any(User.class));
        verify(userMetricHelper).startCreateUserTimer();
        verify(userMetricHelper).stopCreateUserTimer(sample, true);
    }

    @Test
    void createUser_throwsWhenOrgNotFound() {
        // Arrange
        UUID orgId = UUID.randomUUID();
        UserCreateRequest request = new UserCreateRequest();
        request.setOrganizationId(orgId);

        Timer.Sample sample = mock(Timer.Sample.class);
        when(userMetricHelper.startCreateUserTimer()).thenReturn(sample);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());
        // MOCKUJEMO MESSAGEUTIL
        when(messageUtil.get("error.organization.not.found")).thenReturn("error.organization.not.found");

        // Act / Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.organization.not.found");
        verify(userMetricHelper).stopCreateUserTimer(sample, false);
    }

    @Test
    void getUserById_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("abc").build();
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
        UserResponse response = new UserResponse();
        response.setId(userId);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.getUserById(userId);
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void getUserById_notFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());
        when(messageUtil.get("error.user.not.found")).thenReturn("error.user.not.found");

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.user.not.found");
    }

    @Test
    void softDeleteUser_success() {
        UUID userId = UUID.randomUUID();
        UUID performerId = UUID.randomUUID();
        String reason = "Violation";

        User user = spy(User.builder().id(userId).status("ACTIVE").build());
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userValidationService).ensureNotDeleted(user);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = new UserResponse();
        response.setId(userId);
        when(userMapper.toResponse(user)).thenReturn(response);

        // Mock for Timer.Sample
        Timer.Sample sample = mock(Timer.Sample.class);
        when(userMetricHelper.startSoftDeleteUserTimer()).thenReturn(sample);

        UserResponse result = userService.softDeleteUser(userId, performerId, reason);

        assertThat(result.getId()).isEqualTo(userId);
        verify(user).markDeleted();
        verify(userAuditLogHelper).logSoftDeleteUser(user, performerId, reason);
        verify(userNotificationHelper).sendSoftDeleteNotification(user, reason);
        verify(userMetricHelper).startSoftDeleteUserTimer();
        verify(userMetricHelper).stopSoftDeleteUserTimer(sample, true);
    }

    @Test
    void restoreUser_success() {
        UUID userId = UUID.randomUUID();
        UUID performerId = UUID.randomUUID();
        String reason = "Return";

        User user = spy(User.builder().id(userId).status("DELETED").deletedAt(new Date().toInstant()).build());
        when(userRepository.findByIdIncludingDeleted(userId)).thenReturn(Optional.of(user));
        doNothing().when(userValidationService).ensureDeleted(user);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = new UserResponse();
        response.setId(userId);
        when(userMapper.toResponse(user)).thenReturn(response);

        // Mock for Timer.Sample
        Timer.Sample sample = mock(Timer.Sample.class);
        when(userMetricHelper.startRestoreUserTimer()).thenReturn(sample);

        UserResponse result = userService.restoreUser(userId, performerId, reason);

        assertThat(result.getId()).isEqualTo(userId);
        verify(user).restore();
        verify(userAuditLogHelper).logRestoreUser(user, performerId, reason);
        verify(userNotificationHelper).sendRestoreNotification(user, reason);
        verify(userMetricHelper).startRestoreUserTimer();
        verify(userMetricHelper).stopRestoreUserTimer(sample, true);
    }

    @Test
    void getUsersForCurrentTenant_success() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId.toString());
        User user = User.builder().id(UUID.randomUUID()).build();
        when(userRepository.findByOrganizationId(tenantId)).thenReturn(List.of(user));
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        when(userMapper.toResponse(user)).thenReturn(resp);

        List<UserResponse> users = userService.getUsersForCurrentTenant();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(user.getId());
    }

    @Test
    void getUsersForCurrentTenant_throwsIfNotSet() {
        TenantContext.clear();
        when(messageUtil.get("error.tenant.not.set")).thenReturn("error.tenant.not.set");

        assertThatThrownBy(() -> userService.getUsersForCurrentTenant())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("error.tenant.not.set");
    }
}