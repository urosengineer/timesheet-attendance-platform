package com.uros.timesheet.attendance.dto.role;

import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RoleResponse {
    private UUID id;
    private String name;
    private Set<PermissionResponse> permissions;
}