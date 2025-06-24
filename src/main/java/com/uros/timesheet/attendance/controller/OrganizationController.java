package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.organization.OrganizationCreateRequest;
import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import com.uros.timesheet.attendance.dto.organization.OrganizationUpdateRequest;
import com.uros.timesheet.attendance.service.OrganizationService;
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
 * OrganizationController handles management operations for organizations,
 * including creation, retrieval, listing, update, soft deletion, and restoration.
 * <p>
 * All endpoints are secured using JWT authentication and require proper authorization.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Endpoints for managing organizations in the system")
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Creates a new organization.
     *
     * @param request Organization creation payload
     * @return The created organization response
     */
    @Operation(
            summary = "Create a new organization",
            description = "Creates a new organization. Requires ORGANIZATION_CREATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization created successfully",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ORGANIZATION_CREATE') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request) {
        OrganizationResponse created = organizationService.createOrganization(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieves an organization by its unique identifier.
     *
     * @param id Organization UUID
     * @return The organization response
     */
    @Operation(
            summary = "Get organization by ID",
            description = "Retrieves organization details by organization ID. Requires ORGANIZATION_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization found",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> getOrganization(
            @Parameter(description = "Organization unique identifier", required = true)
            @PathVariable UUID id) {
        OrganizationResponse organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    /**
     * Retrieves all active organizations.
     *
     * @return List of all active organizations
     */
    @Operation(
            summary = "List all organizations",
            description = "Retrieves all active organizations. Requires ORGANIZATION_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    /**
     * Updates an existing organization.
     *
     * @param id Organization UUID to update
     * @param request Organization update payload
     * @param performedByUserId UUID of the user performing the action (optional)
     * @return The updated organization response
     */
    @Operation(
            summary = "Update an organization",
            description = "Updates an existing organization by ID. Requires ORGANIZATION_UPDATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization updated successfully",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @Parameter(description = "Organization unique identifier", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationUpdateRequest request,
            @Parameter(description = "ID of the user performing the update", required = false)
            @RequestParam(required = false) UUID performedByUserId) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request, performedByUserId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft deletes an organization (marks as deleted, does not remove from database).
     *
     * @param id Organization UUID to delete
     * @param performedByUserId UUID of the user performing the action (optional)
     * @param reason Reason for deletion (optional)
     * @return The soft-deleted organization response
     */
    @Operation(
            summary = "Soft delete an organization",
            description = "Marks an organization as deleted. Requires ORGANIZATION_DELETE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization soft-deleted successfully",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> softDeleteOrganization(
            @Parameter(description = "Organization unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = false)
            @RequestParam(required = false) UUID performedByUserId,
            @Parameter(description = "Reason for deletion", required = false)
            @RequestParam(required = false) String reason) {
        OrganizationResponse deleted = organizationService.softDeleteOrganization(id, performedByUserId, reason);
        return ResponseEntity.ok(deleted);
    }

    /**
     * Restores a previously soft-deleted organization.
     *
     * @param id Organization UUID to restore
     * @param performedByUserId UUID of the user performing the action (optional)
     * @param reason Reason for restoration (optional)
     * @return The restored organization response
     */
    @Operation(
            summary = "Restore a soft-deleted organization",
            description = "Restores an organization previously marked as deleted. Requires ORGANIZATION_RESTORE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization restored successfully",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ORGANIZATION_RESTORE') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> restoreOrganization(
            @Parameter(description = "Organization unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = false)
            @RequestParam(required = false) UUID performedByUserId,
            @Parameter(description = "Reason for restoration", required = false)
            @RequestParam(required = false) String reason) {
        OrganizationResponse restored = organizationService.restoreOrganization(id, performedByUserId, reason);
        return ResponseEntity.ok(restored);
    }
}