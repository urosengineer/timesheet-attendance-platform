package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.role.RoleCreateRequest;
import com.uros.timesheet.attendance.dto.role.RoleResponse;
import com.uros.timesheet.attendance.dto.role.RoleUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponse createRole(RoleCreateRequest request);
    RoleResponse getRoleById(UUID id);
    List<RoleResponse> getAllRoles();

    RoleResponse updateRole(UUID id, RoleUpdateRequest request, UUID performedBy);
    RoleResponse softDeleteRole(UUID id, UUID performedBy, String reason);
    RoleResponse restoreRole(UUID id, UUID performedBy, String reason);
}