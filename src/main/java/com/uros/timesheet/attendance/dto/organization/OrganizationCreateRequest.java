package com.uros.timesheet.attendance.dto.organization;

import lombok.Data;

@Data
public class OrganizationCreateRequest {
    private String name;
    private String timezone;
}