package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.team.TeamCreateRequest;
import com.uros.timesheet.attendance.dto.team.TeamResponse;
import com.uros.timesheet.attendance.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * TeamController handles team management operations such as creation, retrieval,
 * and listing of teams within the system.
 *
 * All endpoints are secured using JWT and require appropriate authorization.
 */
@RestController
@RequestMapping("/api/v1/teams")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Endpoints for managing teams in the system")
public class TeamController {

    private final TeamService teamService;

    /**
     * Creates a new team in the system.
     *
     * @param request Team creation request payload
     * @return The created team response
     */
    @Operation(
            summary = "Create a new team",
            description = "Creates a new team. Requires TEAM_CREATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team created successfully",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('TEAM_CREATE') or hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody TeamCreateRequest request) {
        TeamResponse created = teamService.createTeam(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieves a team by its unique identifier.
     *
     * @param id Team UUID
     * @return The team response
     */
    @Operation(
            summary = "Get team by ID",
            description = "Retrieves team details by team ID. Requires TEAM_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAM_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> getTeam(
            @Parameter(description = "Team unique identifier", required = true)
            @PathVariable UUID id) {
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    /**
     * Retrieves all teams in the system.
     *
     * @return List of all teams
     */
    @Operation(
            summary = "List all teams",
            description = "Retrieves all teams in the system. Requires TEAM_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('TEAM_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * Retrieves all teams for the current tenant (organization).
     *
     * @return List of teams for the current tenant
     */
    @Operation(
            summary = "List teams for current tenant",
            description = "Retrieves all teams associated with the current tenant. Requires ADMIN or MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/current-tenant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<TeamResponse>> getTeamsForCurrentTenant() {
        List<TeamResponse> teams = teamService.getTeamsForCurrentTenant();
        return ResponseEntity.ok(teams);
    }
}