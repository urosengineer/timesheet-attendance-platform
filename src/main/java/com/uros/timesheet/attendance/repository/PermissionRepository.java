package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findByIdAndDeletedAtIsNull(UUID id);
    List<Permission> findAllByDeletedAtIsNull();
}