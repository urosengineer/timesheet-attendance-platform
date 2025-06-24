package com.uros.timesheet.attendance.event;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogCreateRequest;
import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.service.NotificationService;
import com.uros.timesheet.attendance.service.WorkflowLogService;
import com.uros.timesheet.attendance.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceRecordStatusChangedEventListener {

    private final AuditLogService auditLogService;
    private final WorkflowLogService workflowLogService;
    private final NotificationService notificationService;
    private final MessageUtil messageUtil;

    @EventListener
    public void handle(AttendanceRecordStatusChangedEvent event) {
        // Audit log
        auditLogService.log(
                "ATTENDANCE_STATUS_CHANGE",
                event.getChangedByUserId(),
                messageUtil.get(
                        "audit.attendance.status.changed",
                        event.getAttendanceRecordId(),
                        event.getOldStatus(),
                        event.getNewStatus()
                )
        );

        // Workflow log
        workflowLogService.logTransition(
                WorkflowLogCreateRequest.builder()
                        .relatedEntityType("AttendanceRecord")
                        .relatedEntityId(event.getAttendanceRecordId())
                        .oldStatus(event.getOldStatus())
                        .newStatus(event.getNewStatus())
                        .userId(event.getChangedByUserId())
                        .comment(
                                messageUtil.get("attendance.workflow." + event.getNewStatus().toLowerCase())
                        )
                        .build()
        );

        if ("DELETED".equals(event.getNewStatus()) || "DRAFT".equals(event.getNewStatus())) {
            NotificationCreateRequest notif = new NotificationCreateRequest();
            notif.setRecipientId(event.getUserId());
            notif.setType(NotificationType.WEBSOCKET);
            notif.setTitle(messageUtil.get("notification.attendance.status.title"));
            notif.setMessage(messageUtil.get(
                    "notification.attendance.status.message",
                    event.getNewStatus(),
                    event.getChangedByUserId(),
                    event.getReason() != null ? event.getReason() : ""
            ));
            notificationService.createAndSend(notif);
        }

        log.info("AttendanceRecordStatusChangedEvent processed for record {}: {} → {}",
                event.getAttendanceRecordId(), event.getOldStatus(), event.getNewStatus());
    }
}