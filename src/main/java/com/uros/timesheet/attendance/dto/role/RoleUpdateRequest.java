package com.uros.timesheet.attendance.dto.role;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RoleUpdateRequest {
    private String name;
    private Set<UUID> permissionIds;
}