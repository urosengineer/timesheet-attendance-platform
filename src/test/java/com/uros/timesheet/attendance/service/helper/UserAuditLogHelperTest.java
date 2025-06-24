package com.uros.timesheet.attendance.service.helper;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.UUID;

import static org.mockito.Mockito.*;

class UserAuditLogHelperTest {

    @Mock
    AuditLogService auditLogService;
    @Mock
    MessageUtil messageUtil;

    @InjectMocks
    UserAuditLogHelper helper;

    User user;
    UUID performedByUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        performedByUserId = UUID.randomUUID();
    }

    @Test
    void logCreateUser_logsCorrectly() {
        when(messageUtil.get("audit.user.created", "testuser"))
                .thenReturn("User testuser created.");

        helper.logCreateUser(user);

        verify(auditLogService).log(
                eq("USER_CREATED"),
                eq(user.getId()),
                eq("User testuser created.")
        );
    }

    @Test
    void logSoftDeleteUser_logsCorrectly() {
        when(messageUtil.get("audit.user.softdeleted", "testuser", "reason"))
                .thenReturn("User testuser soft deleted. Reason: reason");

        helper.logSoftDeleteUser(user, performedByUserId, "reason");

        verify(auditLogService).log(
                eq("USER_SOFT_DELETE"),
                eq(performedByUserId),
                eq("User testuser soft deleted. Reason: reason")
        );
    }

    @Test
    void logRestoreUser_logsCorrectly() {
        when(messageUtil.get("audit.user.restored", "testuser", "restored reason"))
                .thenReturn("User testuser restored. Reason: restored reason");

        helper.logRestoreUser(user, performedByUserId, "restored reason");

        verify(auditLogService).log(
                eq("USER_RESTORE"),
                eq(performedByUserId),
                eq("User testuser restored. Reason: restored reason")
        );
    }
}
