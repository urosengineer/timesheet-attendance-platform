package com.uros.timesheet.attendance.dto.attendance;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class AttendanceRecordCreateRequest {
    private UUID userId;
    private UUID organizationId;

    @Schema(example = "2025-06-18", type = "string", format = "date")
    private LocalDate date;

    @Schema(example = "08:30:00", type = "string", format = "time")
    private LocalTime startTime;

    @Schema(example = "17:00:00", type = "string", format = "time")
    private LocalTime endTime;

    private String type;
    private String notes;
}