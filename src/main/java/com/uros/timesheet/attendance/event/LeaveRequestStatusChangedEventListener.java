package com.uros.timesheet.attendance.event;

import com.uros.timesheet.attendance.auditlog.AuditLogService;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogCreateRequest;
import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.enums.NotificationType;
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
public class LeaveRequestStatusChangedEventListener {

    private final AuditLogService auditLogService;
    private final WorkflowLogService workflowLogService;
    private final NotificationService notificationService;
    private final MessageUtil messageUtil;

    @EventListener
    public void handle(LeaveRequestStatusChangedEvent event) {
        // Audit log
        auditLogService.log(
                "LEAVE_REQUEST_STATUS_CHANGE",
                event.getChangedByUserId(),
                messageUtil.get(
                        "audit.leaverequest.status.changed",
                        event.getLeaveRequestId(),
                        event.getOldStatus(),
                        event.getNewStatus()
                )
        );

        // Workflow log
        workflowLogService.logTransition(
                WorkflowLogCreateRequest.builder()
                        .relatedEntityType("LeaveRequest")
                        .relatedEntityId(event.getLeaveRequestId())
                        .oldStatus(event.getOldStatus())
                        .newStatus(event.getNewStatus())
                        .userId(event.getChangedByUserId())
                        .comment(
                                messageUtil.get("leaverequest.workflow." + event.getNewStatus().toLowerCase())
                        )
                        .build()
        );

        if ("DELETED".equals(event.getNewStatus()) || "DRAFT".equals(event.getNewStatus())) {
            NotificationCreateRequest notif = new NotificationCreateRequest();
            notif.setRecipientId(event.getUserId());
            notif.setType(NotificationType.WEBSOCKET);
            notif.setTitle(messageUtil.get("notification.leaverequest.status.title"));
            notif.setMessage(messageUtil.get(
                    "notification.leaverequest.status.message",
                    event.getNewStatus(),
                    event.getChangedByUserId(),
                    event.getReason() != null ? event.getReason() : ""
            ));
            notificationService.createAndSend(notif);
        }

        log.info("LeaveRequestStatusChangedEvent processed for record {}: {} → {}",
                event.getLeaveRequestId(), event.getOldStatus(), event.getNewStatus());
    }
}