package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    @Query("SELECT o FROM Organization o WHERE o.name = :name AND o.deletedAt IS NULL")
    Optional<Organization> findByName(String name);

    @Query("SELECT o FROM Organization o WHERE o.id = :id AND o.deletedAt IS NULL")
    Optional<Organization> findById(UUID id);

    @Query("SELECT o FROM Organization o WHERE o.deletedAt IS NULL")
    List<Organization> findAllActive();

    @Query("SELECT o FROM Organization o WHERE o.id = :id")
    Optional<Organization> findAnyById(UUID id);
}