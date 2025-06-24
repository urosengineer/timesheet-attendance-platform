package com.uros.timesheet.attendance.dto.team;

import lombok.Data;
import java.util.UUID;

@Data
public class TeamCreateRequest {
    private String name;
    private String description;
    private UUID organizationId;
}