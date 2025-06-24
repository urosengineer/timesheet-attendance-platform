package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.id = :id AND ar.deletedAt IS NULL")
    Optional<AttendanceRecord> findById(UUID id);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.id = :userId AND ar.deletedAt IS NULL")
    List<AttendanceRecord> findByUserId(UUID userId);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.id = :userId AND ar.date BETWEEN :startDate AND :endDate AND ar.deletedAt IS NULL")
    List<AttendanceRecord> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.id = :id")
    Optional<AttendanceRecord> findByIdIncludingDeleted(UUID id);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.id = :userId")
    List<AttendanceRecord> findAllByUserIdIncludingDeleted(UUID userId);

    // Multi-tenant filtering: finds all active records for the specified tenant (organization)
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.organization.id = :organizationId AND ar.deletedAt IS NULL")
    List<AttendanceRecord> findByOrganizationId(UUID organizationId);

    // === Metrics queries ===

    @Query("SELECT DISTINCT ar.status FROM AttendanceRecord ar WHERE ar.deletedAt IS NULL")
    List<String> findDistinctStatuses();

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.status = :status AND ar.deletedAt IS NULL")
    long countByStatusAndDeletedAtIsNull(String status);

    @Query("SELECT DISTINCT ar.type FROM AttendanceRecord ar WHERE ar.deletedAt IS NULL")
    List<String> findDistinctTypes();

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.type = :type AND ar.deletedAt IS NULL")
    long countByTypeAndDeletedAtIsNull(String type);

    @Query("SELECT DISTINCT ar.organization.name FROM AttendanceRecord ar WHERE ar.deletedAt IS NULL")
    List<String> findDistinctOrganizationNames();

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.organization.name = :orgName AND ar.deletedAt IS NULL")
    long countByOrganizationName(String orgName);
}