package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.dto.permission.PermissionCreateRequest;
import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import com.uros.timesheet.attendance.dto.permission.PermissionUpdateRequest;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.PermissionMapper;
import com.uros.timesheet.attendance.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.*;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceImplTest {

    @Mock private PermissionRepository permissionRepository;
    @Mock private PermissionMapper permissionMapper;
    @Mock private MessageUtil messageUtil;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPermission_failsIfNameMissing() {
        PermissionCreateRequest req = new PermissionCreateRequest();
        req.setDescription("desc");
        when(messageUtil.get("error.permission.name.required")).thenReturn("Permission name required");
        assertThatThrownBy(() -> permissionService.createPermission(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission name required");
    }

    @Test
    void createPermission_failsIfDescriptionMissing() {
        PermissionCreateRequest req = new PermissionCreateRequest();
        req.setName("USER_EDIT");
        when(messageUtil.get("error.permission.description.required")).thenReturn("Permission description required");
        assertThatThrownBy(() -> permissionService.createPermission(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission description required");
    }

    @Test
    void createPermission_failsIfNameExists() {
        PermissionCreateRequest req = new PermissionCreateRequest();
        req.setName("USER_DELETE");
        req.setDescription("Delete users");
        when(permissionRepository.findByName("USER_DELETE")).thenReturn(Optional.of(new Permission()));
        when(messageUtil.get("error.permission.name.exists")).thenReturn("Permission already exists");
        assertThatThrownBy(() -> permissionService.createPermission(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission already exists");
    }

    @Test
    void getPermissionById_success() {
        UUID id = UUID.randomUUID();
        Permission permission = Permission.builder()
                .id(id)
                .name("USER_EDIT")
                .description("Edit users")
                .build();
        PermissionResponse resp = new PermissionResponse();
        resp.setId(id);
        resp.setName("USER_EDIT");
        resp.setDescription("Edit users");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));
        when(permissionMapper.toResponse(any(Permission.class))).thenReturn(resp);

        PermissionResponse result = permissionService.getPermissionById(id);
        assertThat(result).isEqualTo(resp);
    }

    @Test
    void getPermissionById_notFound() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());
        when(messageUtil.get("error.permission.not.found", id)).thenReturn("Permission not found");
        assertThatThrownBy(() -> permissionService.getPermissionById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission not found");
    }

    @Test
    void getAllPermissions_success() {
        Permission p1 = Permission.builder().id(UUID.randomUUID()).name("A").description("descA").build();
        Permission p2 = Permission.builder().id(UUID.randomUUID()).name("B").description("descB").build();
        PermissionResponse r1 = new PermissionResponse(); r1.setId(p1.getId()); r1.setName("A"); r1.setDescription("descA");
        PermissionResponse r2 = new PermissionResponse(); r2.setId(p2.getId()); r2.setName("B"); r2.setDescription("descB");

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(permissionMapper.toResponse(p1)).thenReturn(r1);
        when(permissionMapper.toResponse(p2)).thenReturn(r2);

        List<PermissionResponse> list = permissionService.getAllPermissions();
        assertThat(list).containsExactly(r1, r2);
    }

    @Test
    void createPermission_success() {
        PermissionCreateRequest req = new PermissionCreateRequest();
        req.setName("USER_VIEW");
        req.setDescription("View users");

        when(permissionRepository.findByName("USER_VIEW")).thenReturn(Optional.empty());

        Permission permission = Permission.builder()
                .id(UUID.randomUUID())
                .name("USER_VIEW")
                .description("View users")
                .build();

        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);

        PermissionResponse resp = new PermissionResponse();
        resp.setId(permission.getId());
        resp.setName("USER_VIEW");
        resp.setDescription("View users");

        when(permissionMapper.toResponse(any(Permission.class))).thenReturn(resp);

        PermissionResponse result = permissionService.createPermission(req);

        assertThat(result.getName()).isEqualTo("USER_VIEW");
        assertThat(result.getDescription()).isEqualTo("View users");
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void updatePermission_failsIfNameExists() {
        UUID id = UUID.randomUUID();
        Permission permission = Permission.builder().id(id).name("A").build();

        PermissionUpdateRequest req = new PermissionUpdateRequest();
        req.setName("B");

        Permission existing = Permission.builder().id(UUID.randomUUID()).name("B").build();

        when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));
        when(permissionRepository.findByName("B")).thenReturn(Optional.of(existing));
        when(messageUtil.get("error.permission.name.exists")).thenReturn("Permission already exists");

        assertThatThrownBy(() -> permissionService.updatePermission(id, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission already exists");
    }

    @Test
    void updatePermission_notFound() {
        UUID id = UUID.randomUUID();
        PermissionUpdateRequest req = new PermissionUpdateRequest();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());
        when(messageUtil.get("error.permission.not.found", id)).thenReturn("Permission not found");
        assertThatThrownBy(() -> permissionService.updatePermission(id, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission not found");
    }

    @Test
    void deletePermission_success() {
        UUID id = UUID.randomUUID();
        Permission permission = Permission.builder().id(id).name("X").description("desc").build();

        PermissionResponse resp = new PermissionResponse();
        resp.setId(id);
        resp.setName("X");
        resp.setDescription("desc");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));
        when(permissionMapper.toResponse(any(Permission.class))).thenReturn(resp);

        PermissionResponse result = permissionService.deletePermission(id);

        assertThat(result).isEqualTo(resp);
        verify(permissionRepository).delete(permission);
    }

    @Test
    void deletePermission_notFound() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());
        when(messageUtil.get("error.permission.not.found", id)).thenReturn("Permission not found");
        assertThatThrownBy(() -> permissionService.deletePermission(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission not found");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
}
