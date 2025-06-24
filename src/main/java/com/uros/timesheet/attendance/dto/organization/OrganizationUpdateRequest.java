package com.uros.timesheet.attendance.dto.organization;

import lombok.Data;

@Data
public class OrganizationUpdateRequest {
    private String name;
    private String timezone;
    private String status;
}