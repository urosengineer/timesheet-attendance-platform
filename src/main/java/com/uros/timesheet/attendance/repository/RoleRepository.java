package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Role> findActiveById(UUID id);
}