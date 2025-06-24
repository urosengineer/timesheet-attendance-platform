package com.uros.timesheet.attendance.dto.leave;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LeaveRequestCreateRequest {
    private UUID userId;
    private UUID organizationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String notes;
}