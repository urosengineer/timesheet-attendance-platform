package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.dto.organization.OrganizationCreateRequest;
import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import com.uros.timesheet.attendance.dto.organization.OrganizationUpdateRequest;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.OrganizationMapper;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrganizationServiceImpl.class)
class OrganizationServiceImplTest {

    @MockBean
    private OrganizationRepository organizationRepository;
    @MockBean
    private OrganizationMapper organizationMapper;
    @MockBean
    private MessageUtil messageUtil;
    @MockBean
    private AuditLogService auditLogService;

    private OrganizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrganizationServiceImpl(
                organizationRepository, organizationMapper, messageUtil, auditLogService
        );
        when(messageUtil.get(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(messageUtil.get(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageUtil.get(anyString(), any(), any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createOrganization_success() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName("ACME");
        request.setTimezone("Europe/Belgrade");

        when(organizationRepository.findByName("ACME")).thenReturn(Optional.empty());

        Organization savedOrg = Organization.builder()
                .id(UUID.randomUUID())
                .name("ACME")
                .timezone("Europe/Belgrade")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        when(organizationRepository.save(any())).thenReturn(savedOrg);

        OrganizationResponse response = new OrganizationResponse();
        response.setId(savedOrg.getId());
        response.setName(savedOrg.getName());
        response.setTimezone(savedOrg.getTimezone());
        response.setStatus(savedOrg.getStatus());

        when(organizationMapper.toResponse(any())).thenReturn(response);

        OrganizationResponse result = service.createOrganization(request);

        assertThat(result.getName()).isEqualTo("ACME");
        assertThat(result.getTimezone()).isEqualTo("Europe/Belgrade");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(organizationRepository).save(orgCaptor.capture());
        Organization saved = orgCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("ACME");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void createOrganization_failsIfNameMissing() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setTimezone("Europe/Belgrade");
        assertThatThrownBy(() -> service.createOrganization(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.name.required");
    }

    @Test
    void createOrganization_failsIfTimezoneMissing() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName("ACME");
        assertThatThrownBy(() -> service.createOrganization(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.timezone.required");
    }

    @Test
    void createOrganization_failsIfNameExists() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName("ACME");
        request.setTimezone("Europe/Belgrade");
        when(organizationRepository.findByName("ACME")).thenReturn(Optional.of(new Organization()));
        assertThatThrownBy(() -> service.createOrganization(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.name.exists");
    }

    @Test
    void getOrganizationById_success() {
        UUID orgId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .name("ACME")
                .timezone("Europe/Belgrade")
                .status("ACTIVE")
                .build();
        OrganizationResponse response = new OrganizationResponse();
        response.setId(orgId);
        response.setName("ACME");
        response.setTimezone("Europe/Belgrade");
        response.setStatus("ACTIVE");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(organizationMapper.toResponse(org)).thenReturn(response);

        OrganizationResponse result = service.getOrganizationById(orgId);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void getOrganizationById_notFound() {
        UUID id = UUID.randomUUID();
        when(organizationRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getOrganizationById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.not.found");
    }

    @Test
    void getAllOrganizations_success() {
        Organization org1 = Organization.builder().id(UUID.randomUUID()).name("A").status("ACTIVE").build();
        Organization org2 = Organization.builder().id(UUID.randomUUID()).name("B").status("ACTIVE").build();
        OrganizationResponse resp1 = new OrganizationResponse();
        resp1.setId(org1.getId());
        resp1.setName(org1.getName());
        resp1.setStatus("ACTIVE");
        OrganizationResponse resp2 = new OrganizationResponse();
        resp2.setId(org2.getId());
        resp2.setName(org2.getName());
        resp2.setStatus("ACTIVE");

        when(organizationRepository.findAllActive()).thenReturn(List.of(org1, org2));
        when(organizationMapper.toResponse(org1)).thenReturn(resp1);
        when(organizationMapper.toResponse(org2)).thenReturn(resp2);

        List<OrganizationResponse> list = service.getAllOrganizations();
        assertThat(list).containsExactly(resp1, resp2);
    }

    @Test
    void updateOrganization_success() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Organization org = Organization.builder()
                .id(orgId)
                .name("Old")
                .timezone("Europe/Belgrade")
                .status("ACTIVE")
                .build();

        OrganizationUpdateRequest req = new OrganizationUpdateRequest();
        req.setName("New");
        req.setTimezone("America/New_York");
        req.setStatus("INACTIVE");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(organizationRepository.findByName("New")).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(org);

        OrganizationResponse response = new OrganizationResponse();
        response.setId(orgId);
        response.setName("New");
        response.setTimezone("America/New_York");
        response.setStatus("INACTIVE");
        when(organizationMapper.toResponse(any())).thenReturn(response);
        when(messageUtil.get(anyString(), any())).thenReturn("Audit message");

        OrganizationResponse result = service.updateOrganization(orgId, req, userId);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getTimezone()).isEqualTo("America/New_York");
        assertThat(result.getStatus()).isEqualTo("INACTIVE");

        verify(auditLogService).log(eq("ORGANIZATION_UPDATE"), eq(userId), eq("Audit message"));
    }

    @Test
    void updateOrganization_failsIfNotFound() {
        UUID orgId = UUID.randomUUID();
        OrganizationUpdateRequest req = new OrganizationUpdateRequest();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateOrganization(orgId, req, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.not.found");
    }

    @Test
    void updateOrganization_failsIfNameExists() {
        UUID orgId = UUID.randomUUID();
        Organization org = Organization.builder().id(orgId).name("Old").build();
        OrganizationUpdateRequest req = new OrganizationUpdateRequest();
        req.setName("ACME");
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(organizationRepository.findByName("ACME")).thenReturn(Optional.of(new Organization()));
        assertThatThrownBy(() -> service.updateOrganization(orgId, req, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.name.exists");
    }

    @Test
    void softDeleteOrganization_success() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .name("ACME")
                .status("ACTIVE")
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(organizationRepository.save(org)).thenReturn(org);

        OrganizationResponse response = new OrganizationResponse();
        response.setId(orgId);
        response.setName("ACME");
        response.setStatus("DELETED");
        when(organizationMapper.toResponse(org)).thenReturn(response);
        when(messageUtil.get(anyString(), any(), any())).thenReturn("Audit deleted");

        OrganizationResponse result = service.softDeleteOrganization(orgId, userId, "test reason");
        assertThat(result.getStatus()).isEqualTo("DELETED");

        // State-based assertions
        assertThat(org.getStatus()).isEqualTo("DELETED");
        assertThat(org.getDeletedAt()).isNotNull();

        verify(auditLogService).log(eq("ORGANIZATION_SOFT_DELETE"), eq(userId), eq("Audit deleted"));
    }

    @Test
    void softDeleteOrganization_failsIfAlreadyDeleted() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .name("ACME")
                .status("DELETED")
                .deletedAt(Instant.now())
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        assertThatThrownBy(() -> service.softDeleteOrganization(orgId, userId, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("error.organization.already.deleted");
    }

    @Test
    void restoreOrganization_success() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .name("ACME")
                .status("DELETED")
                .deletedAt(Instant.now())
                .build();

        when(organizationRepository.findAnyById(orgId)).thenReturn(Optional.of(org));
        when(organizationRepository.save(org)).thenReturn(org);

        OrganizationResponse response = new OrganizationResponse();
        response.setId(orgId);
        response.setName("ACME");
        response.setStatus("ACTIVE");
        when(organizationMapper.toResponse(org)).thenReturn(response);
        when(messageUtil.get(anyString(), any(), any())).thenReturn("Audit restored");

        OrganizationResponse result = service.restoreOrganization(orgId, userId, "restore reason");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        // State-based assertions
        assertThat(org.getStatus()).isEqualTo("ACTIVE");
        assertThat(org.getDeletedAt()).isNull();

        verify(auditLogService).log(eq("ORGANIZATION_RESTORE"), eq(userId), eq("Audit restored"));
    }

    @Test
    void restoreOrganization_failsIfNotDeleted() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .name("ACME")
                .status("ACTIVE")
                .deletedAt(null)
                .build();

        when(organizationRepository.findAnyById(orgId)).thenReturn(Optional.of(org));
        assertThatThrownBy(() -> service.restoreOrganization(orgId, userId, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("error.organization.not.deleted");
    }

    @Test
    void restoreOrganization_failsIfNotFound() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(organizationRepository.findAnyById(orgId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.restoreOrganization(orgId, userId, "reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.not.found");
    }
}