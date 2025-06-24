package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.organization.OrganizationCreateRequest;
import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import com.uros.timesheet.attendance.dto.organization.OrganizationUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    OrganizationResponse createOrganization(OrganizationCreateRequest request);
    OrganizationResponse getOrganizationById(UUID id);
    List<OrganizationResponse> getAllOrganizations();

    OrganizationResponse softDeleteOrganization(UUID id, UUID performedByUserId, String reason);
    OrganizationResponse restoreOrganization(UUID id, UUID performedByUserId, String reason);

    OrganizationResponse updateOrganization(UUID id, OrganizationUpdateRequest request, UUID performedByUserId);
}