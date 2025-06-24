package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.Team;
import com.uros.timesheet.attendance.dto.team.TeamCreateRequest;
import com.uros.timesheet.attendance.dto.team.TeamResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.TeamMapper;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.TeamRepository;
import com.uros.timesheet.attendance.service.TeamService;
import com.uros.timesheet.attendance.util.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamMapper teamMapper;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException(messageUtil.get("error.team.name.required"));
        }
        if (request.getOrganizationId() == null) {
            throw new IllegalArgumentException(messageUtil.get("error.organization.id.required"));
        }
        if (teamRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(messageUtil.get("error.team.name.exists"));
        }
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organization(org)
                .build();
        teamRepository.save(team);
        return teamMapper.toResponse(team);
    }

    @Override
    public TeamResponse getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.team.not.found")));
        return teamMapper.toResponse(team);
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Override
    public List<TeamResponse> getTeamsForCurrentTenant() {
        String tenantIdString = TenantContext.getTenantId();
        if (tenantIdString == null) {
            throw new IllegalStateException(messageUtil.get("error.tenant.not.set"));
        }
        UUID tenantId = UUID.fromString(tenantIdString);
        List<Team> teams = teamRepository.findByOrganizationId(tenantId);
        return teams.stream().map(teamMapper::toResponse).toList();
    }
}
