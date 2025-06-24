package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import com.uros.timesheet.attendance.dto.role.*;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.RoleMapper;
import com.uros.timesheet.attendance.repository.PermissionRepository;
import com.uros.timesheet.attendance.repository.RoleRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private RoleMapper roleMapper;
    @Mock private AuditLogService auditLogService;
    @Mock private MessageUtil messageUtil;

    @InjectMocks
    private RoleServiceImpl roleService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(messageUtil.get("error.role.name.required")).thenReturn("error.role.name.required");
        when(messageUtil.get("error.role.name.exists")).thenReturn("error.role.name.exists");
        when(messageUtil.get(eq("error.role.not.found"), any())).then(inv -> "error.role.not.found:" + inv.getArgument(1));
        when(messageUtil.get(eq("error.permission.not.found"), any())).then(inv -> "error.permission.not.found:" + inv.getArgument(1));
        when(messageUtil.get("error.role.already.deleted")).thenReturn("error.role.already.deleted");
        when(messageUtil.get("error.role.not.deleted")).thenReturn("error.role.not.deleted");

        when(messageUtil.get(eq("audit.role.created"), any()))
                .then(inv -> "Role created: " + inv.getArgument(1));
        when(messageUtil.get(eq("audit.role.updated"), any(), any()))
                .then(inv -> "Role updated: " + inv.getArgument(1) + ", by: " + inv.getArgument(2));
        when(messageUtil.get(eq("audit.role.softdeleted"), any(), any()))
                .then(inv -> "Role softdeleted: " + inv.getArgument(1) + ", reason: " + inv.getArgument(2));
        when(messageUtil.get(eq("audit.role.restored"), any(), any()))
                .then(inv -> "Role restored: " + inv.getArgument(1) + ", reason: " + inv.getArgument(2));
    }


    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createRole_success() {
        UUID permId = UUID.randomUUID();
        RoleCreateRequest req = new RoleCreateRequest();
        req.setName("ADMIN");
        req.setPermissionIds(Set.of(permId));

        Permission perm = Permission.builder().id(permId).name("USER_VIEW").description("View users").build();

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(permissionRepository.findById(permId)).thenReturn(Optional.of(perm));

        Role savedRole = Role.builder().id(UUID.randomUUID()).name("ADMIN").permissions(Set.of(perm)).build();
        when(roleRepository.save(any())).thenReturn(savedRole);

        RoleResponse resp = new RoleResponse();
        resp.setId(savedRole.getId());
        resp.setName("ADMIN");
        PermissionResponse permResp = new PermissionResponse();
        permResp.setId(perm.getId());
        permResp.setName(perm.getName());
        permResp.setDescription(perm.getDescription());
        resp.setPermissions(Set.of(permResp));
        when(roleMapper.toResponse(any(Role.class))).thenReturn(resp);

        RoleResponse result = roleService.createRole(req);

        assertThat(result.getName()).isEqualTo("ADMIN");
        assertThat(result.getPermissions()).hasSize(1);
        verify(roleRepository).save(any(Role.class));
        verify(auditLogService).log(eq("ROLE_CREATE"), isNull(), contains("ADMIN"));
    }

    @Test
    void createRole_failsIfNameMissing() {
        RoleCreateRequest req = new RoleCreateRequest();
        req.setPermissionIds(Set.of());
        assertThatThrownBy(() -> roleService.createRole(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.role.name.required");
    }

    @Test
    void createRole_failsIfNameExists() {
        RoleCreateRequest req = new RoleCreateRequest();
        req.setName("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(new Role()));
        assertThatThrownBy(() -> roleService.createRole(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.role.name.exists");
    }

    @Test
    void createRole_failsIfPermissionNotFound() {
        RoleCreateRequest req = new RoleCreateRequest();
        UUID permId = UUID.randomUUID();
        req.setName("ADMIN");
        req.setPermissionIds(Set.of(permId));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(permissionRepository.findById(permId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roleService.createRole(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.permission.not.found:" + permId);
    }

    @Test
    void getRoleById_success() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.builder().id(roleId).name("USER").build();
        RoleResponse resp = new RoleResponse();
        resp.setId(roleId);
        resp.setName("USER");
        when(roleRepository.findActiveById(roleId)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(resp);
        RoleResponse result = roleService.getRoleById(roleId);
        assertThat(result).isEqualTo(resp);
    }

    @Test
    void getRoleById_notFound() {
        UUID roleId = UUID.randomUUID();
        when(roleRepository.findActiveById(roleId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roleService.getRoleById(roleId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.role.not.found:" + roleId);
    }

    @Test
    void getAllRoles_success() {
        Role role1 = Role.builder().id(UUID.randomUUID()).name("A").build();
        Role role2 = Role.builder().id(UUID.randomUUID()).name("B").build();
        RoleResponse resp1 = new RoleResponse(); resp1.setId(role1.getId()); resp1.setName("A");
        RoleResponse resp2 = new RoleResponse(); resp2.setId(role2.getId()); resp2.setName("B");
        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));
        when(roleMapper.toResponse(role1)).thenReturn(resp1);
        when(roleMapper.toResponse(role2)).thenReturn(resp2);
        List<RoleResponse> list = roleService.getAllRoles();
        assertThat(list).containsExactly(resp1, resp2);
    }

    @Test
    void updateRole_success() {
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();

        Permission perm = Permission.builder().id(permId).name("USER_VIEW").description("View users").build();

        Role role = Role.builder().id(roleId).name("OLD").permissions(new HashSet<>()).build();
        RoleUpdateRequest req = new RoleUpdateRequest();
        req.setName("NEW");
        req.setPermissionIds(Set.of(permId));

        when(roleRepository.findActiveById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(permId)).thenReturn(Optional.of(perm));
        when(roleRepository.save(role)).thenReturn(role);

        RoleResponse resp = new RoleResponse();
        resp.setId(roleId);
        resp.setName("NEW");
        PermissionResponse permResp = new PermissionResponse();
        permResp.setId(permId);
        permResp.setName("USER_VIEW");
        permResp.setDescription("View users");
        resp.setPermissions(Set.of(permResp));
        when(roleMapper.toResponse(role)).thenReturn(resp);

        RoleResponse result = roleService.updateRole(roleId, req, userId);

        assertThat(result.getName()).isEqualTo("NEW");
        assertThat(result.getPermissions()).hasSize(1);
        verify(auditLogService).log(eq("ROLE_UPDATE"), eq(userId), contains("NEW"));
    }

    @Test
    void updateRole_notFound() {
        UUID roleId = UUID.randomUUID();
        RoleUpdateRequest req = new RoleUpdateRequest();
        when(roleRepository.findActiveById(roleId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roleService.updateRole(roleId, req, UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.role.not.found:" + roleId);
    }

    @Test
    void softDeleteRole_success() {
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Role role = Role.builder().id(roleId).name("SOFT_DEL").build();
        when(roleRepository.findActiveById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);

        RoleResponse resp = new RoleResponse(); resp.setId(roleId); resp.setName("SOFT_DEL");
        when(roleMapper.toResponse(role)).thenReturn(resp);

        RoleResponse result = roleService.softDeleteRole(roleId, userId, "test reason");
        assertThat(role.getDeletedAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(roleId);
        verify(auditLogService).log(eq("ROLE_SOFT_DELETE"), eq(userId), contains("SOFT_DEL"));
    }

    @Test
    void softDeleteRole_failsIfAlreadyDeleted() {
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Role role = Role.builder().id(roleId).name("X").deletedAt(Instant.now()).build();
        when(roleRepository.findActiveById(roleId)).thenReturn(Optional.of(role));
        assertThatThrownBy(() -> roleService.softDeleteRole(roleId, userId, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("error.role.already.deleted");
    }

    @Test
    void restoreRole_success() {
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Role role = Role.builder().id(roleId).name("REST").deletedAt(Instant.now()).build();
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);

        RoleResponse resp = new RoleResponse(); resp.setId(roleId); resp.setName("REST");
        when(roleMapper.toResponse(role)).thenReturn(resp);

        RoleResponse result = roleService.restoreRole(roleId, userId, "restored");
        assertThat(role.getDeletedAt()).isNull();
        verify(auditLogService).log(eq("ROLE_RESTORE"), eq(userId), contains("REST"));
    }

    @Test
    void restoreRole_failsIfNotDeleted() {
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Role role = Role.builder().id(roleId).name("ACTIVE").deletedAt(null).build();
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        assertThatThrownBy(() -> roleService.restoreRole(roleId, userId, "restored"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("error.role.not.deleted");
    }

    @Test
    void restoreRole_failsIfNotFound() {
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roleService.restoreRole(roleId, userId, "restored"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.role.not.found:" + roleId);
    }
}