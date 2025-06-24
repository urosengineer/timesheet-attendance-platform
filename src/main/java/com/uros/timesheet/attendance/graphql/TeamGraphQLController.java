package com.uros.timesheet.attendance.graphql;

import com.uros.timesheet.attendance.dto.team.TeamResponse;
import com.uros.timesheet.attendance.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TeamGraphQLController {

    private final TeamService teamService;

    @QueryMapping
    public TeamResponse team(@Argument UUID id) {
        return teamService.getTeamById(id);
    }

    @QueryMapping
    public List<TeamResponse> teams() {
        return teamService.getAllTeams();
    }
}