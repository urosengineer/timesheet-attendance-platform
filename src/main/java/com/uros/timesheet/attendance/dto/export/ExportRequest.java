package com.uros.timesheet.attendance.dto.export;

import com.uros.timesheet.attendance.domain.User;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ExportRequest {
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExportType exportType;
    private User requestedBy;

    public enum ExportType {
        ATTENDANCE, LEAVE_REQUESTS, ALL
    }
}