package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.LeaveRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    @EntityGraph(attributePaths = {"user", "organization", "approver"})
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :id AND lr.deletedAt IS NULL")
    Optional<LeaveRequest> findById(UUID id);

    @EntityGraph(attributePaths = {"user", "organization", "approver"})
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :id")
    Optional<LeaveRequest> findByIdIncludingDeleted(UUID id);

    @EntityGraph(attributePaths = {"user", "organization", "approver"})
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.deletedAt IS NULL")
    List<LeaveRequest> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "organization", "approver"})
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.organization.id = :organizationId AND lr.deletedAt IS NULL")
    List<LeaveRequest> findByOrganizationId(UUID organizationId);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.user.id = :userId
          AND lr.startDate >= :from
          AND lr.endDate <= :to
          AND lr.deletedAt IS NULL
    """)
    List<LeaveRequest> findByUserIdAndStartDateBetween(UUID userId, LocalDate from, LocalDate to);
}