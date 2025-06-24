package com.uros.timesheet.attendance.dto.export;

import lombok.Data;

@Data
public class LeaveRequestExportDto {
    private String startDate;
    private String endDate;
    private String type;
    private String status;
    private String approver;
    private String notes;
}