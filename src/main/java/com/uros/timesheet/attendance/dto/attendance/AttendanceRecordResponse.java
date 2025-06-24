package com.uros.timesheet.attendance.dto.attendance;

import com.uros.timesheet.attendance.dto.user.UserResponse;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.UUID;

@Data
public class AttendanceRecordResponse {
    private UUID id;
    private UserResponse user;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String type;
    private String status;
    private UserResponse approver;
    private Instant approvedAt;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}