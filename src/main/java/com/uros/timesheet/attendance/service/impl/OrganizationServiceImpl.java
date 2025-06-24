package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.dto.organization.OrganizationCreateRequest;
import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import com.uros.timesheet.attendance.dto.organization.OrganizationUpdateRequest;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.OrganizationMapper;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.service.OrganizationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final MessageUtil messageUtil;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationCreateRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException(messageUtil.get("error.organization.name.required"));
        }
        if (!StringUtils.hasText(request.getTimezone())) {
            throw new IllegalArgumentException(messageUtil.get("error.organization.timezone.required"));
        }
        if (organizationRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(messageUtil.get("error.organization.name.exists"));
        }
        Organization org = Organization.builder()
                .name(request.getName())
                .timezone(request.getTimezone())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();
        organizationRepository.save(org);
        return organizationMapper.toResponse(org);
    }

    @Override
    public OrganizationResponse getOrganizationById(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));
        return organizationMapper.toResponse(org);
    }

    @Override
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAllActive().stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(UUID id, OrganizationUpdateRequest request, UUID performedByUserId) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));

        if (request.getName() != null && !request.getName().equals(org.getName())) {
            if (organizationRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException(messageUtil.get("error.organization.name.exists"));
            }
            org.setName(request.getName());
        }
        if (request.getTimezone() != null) {
            org.setTimezone(request.getTimezone());
        }
        if (request.getStatus() != null) {
            org.setStatus(request.getStatus());
        }
        organizationRepository.save(org);

        auditLogService.log(
                "ORGANIZATION_UPDATE",
                performedByUserId,
                messageUtil.get("audit.organization.updated", org.getName())
        );

        return organizationMapper.toResponse(org);
    }

    @Override
    @Transactional
    public OrganizationResponse softDeleteOrganization(UUID id, UUID performedByUserId, String reason) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));
        if (org.isDeleted()) {
            throw new IllegalStateException(messageUtil.get("error.organization.already.deleted"));
        }
        org.markDeleted();
        organizationRepository.save(org);

        auditLogService.log(
                "ORGANIZATION_SOFT_DELETE",
                performedByUserId,
                messageUtil.get("audit.organization.softdeleted", org.getName(), reason)
        );

        return organizationMapper.toResponse(org);
    }

    @Override
    @Transactional
    public OrganizationResponse restoreOrganization(UUID id, UUID performedByUserId, String reason) {
        Organization org = organizationRepository.findAnyById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));
        if (!org.isDeleted()) {
            throw new IllegalStateException(messageUtil.get("error.organization.not.deleted"));
        }
        org.restore();
        organizationRepository.save(org);

        auditLogService.log(
                "ORGANIZATION_RESTORE",
                performedByUserId,
                messageUtil.get("audit.organization.restored", org.getName(), reason)
        );

        return organizationMapper.toResponse(org);
    }
}