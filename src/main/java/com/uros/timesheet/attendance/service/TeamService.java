package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.team.TeamCreateRequest;
import com.uros.timesheet.attendance.dto.team.TeamResponse;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    TeamResponse createTeam(TeamCreateRequest request);
    TeamResponse getTeamById(UUID id);
    List<TeamResponse> getAllTeams();

    List<TeamResponse> getTeamsForCurrentTenant();
}