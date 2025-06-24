package com.uros.timesheet.attendance.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main orchestrator for database seeding.
 */
@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final OrganizationSeeder organizationSeeder;
    private final PermissionSeeder permissionSeeder;
    private final RoleSeeder roleSeeder;
    private final TeamSeeder teamSeeder;
    private final UserSeeder userSeeder;
    private final AttendanceSeeder attendanceSeeder;
    private final LeaveRequestSeeder leaveRequestSeeder;
    private final NotificationSeeder notificationSeeder;
    private final AuditLogSeeder auditLogSeeder;
    private final WorkflowSeeder workflowSeeder;

    @Bean
    public CommandLineRunner runAllSeeders() {
        return args -> {
            organizationSeeder.seedIfTableEmpty();
            permissionSeeder.seedIfTableEmpty();
            roleSeeder.seedIfTableEmpty();
            teamSeeder.seedIfTableEmpty();
            userSeeder.seedIfTableEmpty();
            attendanceSeeder.seedIfTableEmpty();
            leaveRequestSeeder.seedIfTableEmpty();
            notificationSeeder.seedIfTableEmpty();
            auditLogSeeder.seedIfTableEmpty();
            workflowSeeder.seedIfTableEmpty();
        };
    }
}