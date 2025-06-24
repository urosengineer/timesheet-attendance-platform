package com.uros.timesheet.attendance.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserAttendanceSummaryDto {
    private UUID userId;
    private String userFullName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalDays;
    private long totalRecords;
    private BigDecimal totalHours;
}