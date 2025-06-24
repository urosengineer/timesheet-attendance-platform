package com.uros.timesheet.attendance.dto.team;

import lombok.Data;
import java.util.UUID;

@Data
public class TeamResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID organizationId;
    private String organizationName;
}