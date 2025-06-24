package com.uros.timesheet.attendance.dto.leave;

import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import lombok.Data;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Data
public class LeaveRequestResponse {
    private UUID id;
    private UserResponse user;
    private OrganizationResponse organization;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String status;
    private UserResponse approver;
    private Instant approvedAt;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}