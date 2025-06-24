package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.permission.PermissionCreateRequest;
import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import com.uros.timesheet.attendance.dto.permission.PermissionUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionResponse createPermission(PermissionCreateRequest request);
    PermissionResponse getPermissionById(UUID id);
    List<PermissionResponse> getAllPermissions();
    PermissionResponse updatePermission(UUID id, PermissionUpdateRequest request);
    PermissionResponse deletePermission(UUID id);
}