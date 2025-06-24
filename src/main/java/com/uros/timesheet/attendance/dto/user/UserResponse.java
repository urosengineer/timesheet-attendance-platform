package com.uros.timesheet.attendance.dto.user;

import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import com.uros.timesheet.attendance.dto.role.RoleResponse;
import com.uros.timesheet.attendance.dto.team.TeamResponse;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String status;
    private OrganizationResponse organization;
    private TeamResponse team;
    private Set<RoleResponse> roles;
    private Instant deletedAt;
}