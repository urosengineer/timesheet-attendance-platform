package com.uros.timesheet.attendance.dto.permission;

import lombok.Data;

import java.util.UUID;

@Data
public class PermissionResponse {
    private UUID id;
    private String name;
    private String description;
}