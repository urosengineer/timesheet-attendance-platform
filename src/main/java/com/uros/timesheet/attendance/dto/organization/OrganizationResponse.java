package com.uros.timesheet.attendance.dto.organization;

import lombok.Data;

import java.util.UUID;

@Data
public class OrganizationResponse {
    private UUID id;
    private String name;
    private String timezone;
    private String status;
}