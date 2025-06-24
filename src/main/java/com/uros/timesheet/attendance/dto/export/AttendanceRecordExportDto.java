package com.uros.timesheet.attendance.dto.export;

import lombok.Data;

@Data
public class AttendanceRecordExportDto {
    private String date;
    private String startTime;
    private String endTime;
    private String type;
    private String status;
    private String notes;
}