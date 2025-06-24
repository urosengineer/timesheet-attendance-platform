package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionSeeder {

    private final PermissionRepository permissionRepository;

    public void seedIfTableEmpty() {
        if (permissionRepository.count() == 0) {
            List<Permission> permissions = List.of(
                    new Permission(null, "USER_CREATE", "Create users", null),
                    new Permission(null, "USER_VIEW", "View users", null),
                    new Permission(null, "USER_EDIT", "Edit users", null),
                    new Permission(null, "USER_DELETE", "Delete users", null),
                    new Permission(null, "ATTENDANCE_CREATE", "Create attendance records", null),
                    new Permission(null, "ATTENDANCE_VIEW", "View attendance records", null),
                    new Permission(null, "ATTENDANCE_APPROVE", "Approve attendance", null),
                    new Permission(null, "ATTENDANCE_REJECT", "Reject attendance", null),
                    new Permission(null, "LEAVE_REQUEST_CREATE", "Create leave request", null),
                    new Permission(null, "LEAVE_REQUEST_SUBMIT", "Submit leave request", null),
                    new Permission(null, "LEAVE_REQUEST_APPROVE", "Approve leave request", null),
                    new Permission(null, "LEAVE_REQUEST_REJECT", "Reject leave request", null),
                    new Permission(null, "LEAVE_REQUEST_VIEW", "View leave requests", null),
                    new Permission(null, "LEAVE_REQUEST_VIEW_SELF", "View own leave requests", null),
                    new Permission(null, "LEAVE_REQUEST_DELETE", "Delete leave request", null),
                    new Permission(null, "LEAVE_REQUEST_RESTORE", "Restore leave request", null),
                    new Permission(null, "REPORT_VIEW", "View reports", null),
                    new Permission(null, "EXPORT_PDF", "Export data to PDF", null),
                    new Permission(null, "EXPORT_EXCEL", "Export data to Excel", null),
                    new Permission(null, "ADMIN_PANEL_ACCESS", "Access admin panel", null)
            );
            permissionRepository.saveAll(permissions);
        }
    }
}