package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.dto.role.RoleCreateRequest;
import com.uros.timesheet.attendance.dto.role.RoleResponse;
import com.uros.timesheet.attendance.dto.role.RoleUpdateRequest;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.RoleMapper;
import com.uros.timesheet.attendance.repository.PermissionRepository;
import com.uros.timesheet.attendance.repository.RoleRepository;
import com.uros.timesheet.attendance.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final AuditLogService auditLogService;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException(messageUtil.get("error.role.name.required"));
        }
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(messageUtil.get("error.role.name.exists"));
        }
        Set<Permission> permissions = resolvePermissions(request.getPermissionIds());

        Role role = Role.builder()
                .name(request.getName())
                .permissions(permissions)
                .build();
        roleRepository.save(role);

        auditLogService.log("ROLE_CREATE", null, messageUtil.get("audit.role.created", role.getName()));
        return roleMapper.toResponse(role);
    }

    @Override
    public RoleResponse getRoleById(UUID id) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException(messageUtil.get("error.role.not.found", id)));
        return roleMapper.toResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, RoleUpdateRequest request, UUID performedBy) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException(messageUtil.get("error.role.not.found", id)));
        if (StringUtils.hasText(request.getName())) {
            role.setName(request.getName());
        }
        if (request.getPermissionIds() != null) {
            role.setPermissions(resolvePermissions(request.getPermissionIds()));
        }
        roleRepository.save(role);

        auditLogService.log("ROLE_UPDATE", performedBy,
                messageUtil.get("audit.role.updated", role.getName(), performedBy));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse softDeleteRole(UUID id, UUID performedBy, String reason) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException(messageUtil.get("error.role.not.found", id)));
        if (role.getDeletedAt() != null) {
            throw new IllegalStateException(messageUtil.get("error.role.already.deleted"));
        }
        role.setDeletedAt(Instant.now());
        roleRepository.save(role);

        auditLogService.log("ROLE_SOFT_DELETE", performedBy,
                messageUtil.get("audit.role.softdeleted", role.getName(), reason));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse restoreRole(UUID id, UUID performedBy, String reason) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageUtil.get("error.role.not.found", id)));
        if (role.getDeletedAt() == null) {
            throw new IllegalStateException(messageUtil.get("error.role.not.deleted"));
        }
        role.setDeletedAt(null);
        roleRepository.save(role);

        auditLogService.log("ROLE_RESTORE", performedBy,
                messageUtil.get("audit.role.restored", role.getName(), reason));
        return roleMapper.toResponse(role);
    }

    private Set<Permission> resolvePermissions(Set<UUID> permissionIds) {
        Set<Permission> permissions = new HashSet<>();
        if (permissionIds != null) {
            for (UUID permId : permissionIds) {
                Permission perm = permissionRepository.findById(permId)
                        .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.permission.not.found", permId)));
                permissions.add(perm);
            }
        }
        return permissions;
    }
}