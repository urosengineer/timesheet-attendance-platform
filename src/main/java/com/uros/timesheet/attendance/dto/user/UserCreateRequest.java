package com.uros.timesheet.attendance.dto.user;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private UUID organizationId;
    private UUID teamId;
    private Set<UUID> roleIds;
}