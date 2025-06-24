package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(String name);

    List<Team> findByOrganizationId(UUID organizationId);
}