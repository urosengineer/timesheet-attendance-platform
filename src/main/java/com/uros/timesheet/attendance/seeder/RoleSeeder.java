package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.repository.PermissionRepository;
import com.uros.timesheet.attendance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public void seedIfTableEmpty() {
        if (roleRepository.count() == 0) {
            Set<Permission> allPerms = new HashSet<>(permissionRepository.findAll());

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .permissions(allPerms)
                    .build();
            roleRepository.save(adminRole);

            Set<Permission> managerPerms = Set.of(
                    permissionRepository.findByName("USER_VIEW").orElseThrow(),
                    permissionRepository.findByName("ATTENDANCE_CREATE").orElseThrow(),
                    permissionRepository.findByName("ATTENDANCE_VIEW").orElseThrow(),
                    permissionRepository.findByName("ATTENDANCE_APPROVE").orElseThrow(),
                    permissionRepository.findByName("ATTENDANCE_REJECT").orElseThrow(),
                    permissionRepository.findByName("LEAVE_REQUEST_CREATE").orElseThrow(),
                    permissionRepository.findByName("LEAVE_REQUEST_APPROVE").orElseThrow(),
                    permissionRepository.findByName("LEAVE_REQUEST_VIEW").orElseThrow(),
                    permissionRepository.findByName("REPORT_VIEW").orElseThrow(),
                    permissionRepository.findByName("EXPORT_PDF").orElseThrow(),
                    permissionRepository.findByName("EXPORT_EXCEL").orElseThrow()
            );
            roleRepository.save(Role.builder()
                    .name("MANAGER")
                    .permissions(managerPerms)
                    .build());

            Set<Permission> employeePerms = Set.of(
                    permissionRepository.findByName("ATTENDANCE_CREATE").orElseThrow(),
                    permissionRepository.findByName("ATTENDANCE_VIEW").orElseThrow(),
                    permissionRepository.findByName("LEAVE_REQUEST_CREATE").orElseThrow(),
                    permissionRepository.findByName("LEAVE_REQUEST_VIEW").orElseThrow()
            );
            roleRepository.save(Role.builder()
                    .name("EMPLOYEE")
                    .permissions(employeePerms)
                    .build());
        }
    }
}