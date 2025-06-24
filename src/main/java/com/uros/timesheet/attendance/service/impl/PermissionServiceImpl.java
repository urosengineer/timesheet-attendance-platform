package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.dto.permission.PermissionCreateRequest;
import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.PermissionMapper;
import com.uros.timesheet.attendance.repository.PermissionRepository;
import com.uros.timesheet.attendance.service.PermissionService;
import com.uros.timesheet.attendance.dto.permission.PermissionUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException(messageUtil.get("error.permission.name.required"));
        }
        if (!StringUtils.hasText(request.getDescription())) {
            throw new IllegalArgumentException(messageUtil.get("error.permission.description.required"));
        }
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(messageUtil.get("error.permission.name.exists"));
        }
        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        permissionRepository.save(permission);
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(UUID id, PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.permission.not.found", id)));
        if (StringUtils.hasText(request.getName())) {
            // provera da li postoji već permission sa tim imenom, osim trenutnog
            permissionRepository.findByName(request.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(messageUtil.get("error.permission.name.exists"));
                    });
            permission.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            permission.setDescription(request.getDescription());
        }
        permissionRepository.save(permission);
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.permission.not.found", id)));
        permissionRepository.delete(permission);
        return permissionMapper.toResponse(permission);
    }

    @Override
    public PermissionResponse getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.permission.not.found", id)));
        return permissionMapper.toResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .toList();
    }
}