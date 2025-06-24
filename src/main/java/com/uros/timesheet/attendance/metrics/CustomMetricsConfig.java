package com.uros.timesheet.attendance.metrics;

import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class CustomMetricsConfig {

    private final MeterRegistry meterRegistry;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void registerMetrics() {
        registerAttendanceMetrics();
        registerUserMetrics();
    }

    private void registerAttendanceMetrics() {
        // Total number of attendance records
        meterRegistry.gauge("attendance_records.total", attendanceRecordRepository, AttendanceRecordRepository::count);

        // Number by status (DRAFT, SUBMITTED, APPROVED, ...)
        for (String status : attendanceRecordRepository.findDistinctStatuses()) {
            meterRegistry.gauge(
                    "attendance_records.by_status",
                    Tags.of("status", status),
                    attendanceRecordRepository,
                    repo -> repo.countByStatusAndDeletedAtIsNull(status)
            );
        }

        // Number by type (work, remote, sick, leave)
        for (String type : attendanceRecordRepository.findDistinctTypes()) {
            meterRegistry.gauge(
                    "attendance_records.by_type",
                    Tags.of("type", type),
                    attendanceRecordRepository,
                    repo -> repo.countByTypeAndDeletedAtIsNull(type)
            );
        }

        // Number by organization (by name)
        for (String orgName : attendanceRecordRepository.findDistinctOrganizationNames()) {
            meterRegistry.gauge(
                    "attendance_records.by_org",
                    Tags.of("organization", orgName),
                    attendanceRecordRepository,
                    repo -> repo.countByOrganizationName(orgName)
            );
        }
    }

    private void registerUserMetrics() {
        // Total number of active (non-deleted) users
        meterRegistry.gauge("user.total", Tags.empty(), userRepository, repo -> (double) repo.countActive());

        // Number of users by status (ACTIVE, INACTIVE, DELETED, ...)
        Set<String> statuses = new HashSet<>();
        userRepository.findAllIncludingDeleted().forEach(u -> statuses.add(u.getStatus()));
        for (String status : statuses) {
            meterRegistry.gauge(
                    "user.by_status",
                    Tags.of("status", status),
                    userRepository,
                    repo -> (int) repo.findAllIncludingDeleted().stream()
                            .filter(u -> status.equals(u.getStatus()))
                            .count()
            );
        }

        // Number of users by organization (by name)
        Set<String> orgNames = new HashSet<>();
        userRepository.findAllActive().forEach(u -> orgNames.add(u.getOrganization().getName()));
        for (String org : orgNames) {
            meterRegistry.gauge(
                    "user.by_organization",
                    Tags.of("organization", org),
                    userRepository,
                    repo -> (int) repo.findAllActive().stream()
                            .filter(u -> org.equals(u.getOrganization().getName()))
                            .count()
            );
        }

        // Number of users by role (role name)
        Set<String> roles = new HashSet<>();
        userRepository.findAllActive().forEach(u -> u.getRoles().forEach(r -> roles.add(r.getName())));
        for (String role : roles) {
            meterRegistry.gauge(
                    "user.by_role",
                    Tags.of("role", role),
                    userRepository,
                    repo -> (int) repo.findAllActive().stream()
                            .filter(u -> u.getRoles().stream().anyMatch(r -> role.equals(r.getName())))
                            .count()
            );
        }
    }
}