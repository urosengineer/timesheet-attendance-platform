package com.uros.timesheet.attendance.service.helper;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAuditLogHelper {

    private final AuditLogService auditLogService;
    private final MessageUtil messageUtil;

    public void logCreateUser(User user) {
        auditLogService.log(
                "USER_CREATED",
                user.getId(),
                messageUtil.get("audit.user.created", user.getUsername())
        );
    }

    public void logSoftDeleteUser(User user, UUID performedByUserId, String reason) {
        auditLogService.log(
                "USER_SOFT_DELETE",
                performedByUserId,
                messageUtil.get("audit.user.softdeleted", user.getUsername(), reason)
        );
    }

    public void logRestoreUser(User user, UUID performedByUserId, String reason) {
        auditLogService.log(
                "USER_RESTORE",
                performedByUserId,
                messageUtil.get("audit.user.restored", user.getUsername(), reason)
        );
    }
}