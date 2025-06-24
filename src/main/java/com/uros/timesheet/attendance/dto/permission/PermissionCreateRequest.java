package com.uros.timesheet.attendance.dto.permission;

import lombok.Data;

@Data
public class PermissionCreateRequest {
    private String name;
    private String description;
}