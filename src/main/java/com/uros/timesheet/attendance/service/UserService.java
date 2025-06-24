package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.user.UserCreateRequest;
import com.uros.timesheet.attendance.dto.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(UUID id);
    UserResponse softDeleteUser(UUID id, UUID performedByUserId, String reason);
    UserResponse restoreUser(UUID id, UUID performedByUserId, String reason);

    List<UserResponse> getUsersForCurrentTenant();
}