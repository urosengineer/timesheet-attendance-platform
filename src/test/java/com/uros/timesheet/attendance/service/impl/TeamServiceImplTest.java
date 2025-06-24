package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.Team;
import com.uros.timesheet.attendance.dto.team.TeamCreateRequest;
import com.uros.timesheet.attendance.dto.team.TeamResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.TeamMapper;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.TeamRepository;
import com.uros.timesheet.attendance.util.TenantContext;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private TeamMapper teamMapper;
    @Mock private MessageUtil messageUtil;

    @InjectMocks
    private TeamServiceImpl teamService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createTeam_success() {
        // Arrange
        TeamCreateRequest req = new TeamCreateRequest();
        req.setName("Tech");
        req.setDescription("Desc");
        UUID orgId = UUID.randomUUID();
        req.setOrganizationId(orgId);

        when(teamRepository.findByName("Tech")).thenReturn(Optional.empty());

        Organization org = Organization.builder()
                .id(orgId)
                .name("Org")
                .timezone("CET")
                .status("ACTIVE")
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("Tech")
                .description("Desc")
                .organization(org)
                .build();

        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse resp = new TeamResponse();
        resp.setId(team.getId());
        resp.setName("Tech");
        resp.setDescription("Desc");
        resp.setOrganizationId(orgId);
        resp.setOrganizationName("Org");

        when(teamMapper.toResponse(any(Team.class))).thenReturn(resp);

        // Act
        TeamResponse result = teamService.createTeam(req);

        // Assert
        assertThat(result.getName()).isEqualTo("Tech");
        assertThat(result.getOrganizationName()).isEqualTo("Org");
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void createTeam_failsIfNameMissing() {
        TeamCreateRequest req = new TeamCreateRequest();
        req.setOrganizationId(UUID.randomUUID());
        when(messageUtil.get("error.team.name.required")).thenReturn("Team name required");
        assertThatThrownBy(() -> teamService.createTeam(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team name required");
    }

    @Test
    void createTeam_failsIfOrgIdMissing() {
        TeamCreateRequest req = new TeamCreateRequest();
        req.setName("DevOps");
        when(messageUtil.get("error.organization.id.required")).thenReturn("Organization id required");
        assertThatThrownBy(() -> teamService.createTeam(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization id required");
    }

    @Test
    void createTeam_failsIfNameExists() {
        TeamCreateRequest req = new TeamCreateRequest();
        req.setName("QA");
        req.setOrganizationId(UUID.randomUUID());
        when(teamRepository.findByName("QA")).thenReturn(Optional.of(new Team()));
        when(messageUtil.get("error.team.name.exists")).thenReturn("Team already exists");
        assertThatThrownBy(() -> teamService.createTeam(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team already exists");
    }

    @Test
    void createTeam_failsIfOrganizationNotFound() {
        TeamCreateRequest req = new TeamCreateRequest();
        req.setName("Sec");
        UUID orgId = UUID.randomUUID();
        req.setOrganizationId(orgId);
        when(teamRepository.findByName("Sec")).thenReturn(Optional.empty());
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());
        when(messageUtil.get("error.organization.not.found")).thenReturn("Organization not found");
        assertThatThrownBy(() -> teamService.createTeam(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization not found");
    }

    @Test
    void getTeamById_success() {
        UUID id = UUID.randomUUID();
        Team team = Team.builder().id(id).name("Product").description("Desc").build();
        TeamResponse resp = new TeamResponse();
        resp.setId(id); resp.setName("Product"); resp.setDescription("Desc");
        when(teamRepository.findById(id)).thenReturn(Optional.of(team));
        when(teamMapper.toResponse(team)).thenReturn(resp);

        TeamResponse result = teamService.getTeamById(id);
        assertThat(result).isEqualTo(resp);
    }

    @Test
    void getTeamById_notFound() {
        UUID id = UUID.randomUUID();
        when(teamRepository.findById(id)).thenReturn(Optional.empty());
        when(messageUtil.get("error.team.not.found")).thenReturn("Team not found");
        assertThatThrownBy(() -> teamService.getTeamById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team not found");
    }

    @Test
    void getAllTeams_success() {
        Team t1 = Team.builder().id(UUID.randomUUID()).name("A").description("a").build();
        Team t2 = Team.builder().id(UUID.randomUUID()).name("B").description("b").build();
        TeamResponse r1 = new TeamResponse(); r1.setId(t1.getId()); r1.setName("A"); r1.setDescription("a");
        TeamResponse r2 = new TeamResponse(); r2.setId(t2.getId()); r2.setName("B"); r2.setDescription("b");
        when(teamRepository.findAll()).thenReturn(List.of(t1, t2));
        when(teamMapper.toResponse(t1)).thenReturn(r1);
        when(teamMapper.toResponse(t2)).thenReturn(r2);

        List<TeamResponse> result = teamService.getAllTeams();
        assertThat(result).containsExactly(r1, r2);
    }

    @Test
    void getTeamsForCurrentTenant_success() {
        UUID orgId = UUID.randomUUID();
        // Simulate static method
        try (MockedStatic<TenantContext> tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getTenantId).thenReturn(orgId.toString());

            Team t1 = Team.builder().id(UUID.randomUUID()).name("A").description("a").build();
            Team t2 = Team.builder().id(UUID.randomUUID()).name("B").description("b").build();
            when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(t1, t2));
            TeamResponse r1 = new TeamResponse(); r1.setId(t1.getId()); r1.setName("A");
            TeamResponse r2 = new TeamResponse(); r2.setId(t2.getId()); r2.setName("B");
            when(teamMapper.toResponse(t1)).thenReturn(r1);
            when(teamMapper.toResponse(t2)).thenReturn(r2);

            List<TeamResponse> result = teamService.getTeamsForCurrentTenant();
            assertThat(result).containsExactly(r1, r2);
        }
    }

    @Test
    void getTeamsForCurrentTenant_throwsIfNotSet() {
        try (MockedStatic<TenantContext> tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getTenantId).thenReturn(null);
            when(messageUtil.get("error.tenant.not.set")).thenReturn("Tenant not set");
            assertThatThrownBy(() -> teamService.getTeamsForCurrentTenant())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Tenant not set");
        }
    }
}