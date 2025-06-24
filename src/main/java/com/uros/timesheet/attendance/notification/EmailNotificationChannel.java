package com.uros.timesheet.attendance.notification;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.enums.NotificationType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final MeterRegistry meterRegistry;

    @Value("${notifications.email.from:noreply@example.com}")
    private String fromAddress;

    // Setter for test environments
    void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public boolean send(Notification notification) {
        Timer.Sample timerSample = Timer.start(meterRegistry);

        boolean sentOk = false;
        try {
            String template = resolveTemplate(notification);
            Context context = buildContext(notification);
            String htmlBody = emailTemplateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(notification.getRecipient().getEmail());
            helper.setSubject(notification.getTitle());
            helper.setText(htmlBody, true);

            mailSender.send(message);

            log.info("[EmailNotificationChannel] Email successfully sent to {}: {}", notification.getRecipient().getEmail(), notification.getTitle());
            sentOk = true;
            return true;
        } catch (Exception ex) {
            log.error("[EmailNotificationChannel] Failed to send email to {}: {}", notification.getRecipient().getEmail(), ex.getMessage(), ex);
            return false;
        } finally {
            timerSample.stop(
                    meterRegistry.timer(
                            "notifications.email.latency",
                            "status", sentOk ? "SENT" : "FAILED"
                    )
            );
            meterRegistry.counter(
                    "notifications.email.count",
                    "status", sentOk ? "SENT" : "FAILED"
            ).increment();
        }
    }

    private String resolveTemplate(Notification notification) {
        if (notification.getType() != null) {
            switch (notification.getType()) {
                case LEAVE:
                    return "leave-request-status-changed";
                case ATTENDANCE:
                    return "attendance-status-changed";
                case WEBSOCKET:
                    return "websocket-notification";
                case EMAIL:
                    return "generic-notification";
                default:
                    return "generic-notification";
            }
        }
        String title = notification.getTitle() != null ? notification.getTitle().toLowerCase() : "";
        if (title.contains("leave")) {
            return "leave-request-status-changed";
        }
        if (title.contains("attendance")) {
            return "attendance-status-changed";
        }
        return "generic-notification";
    }

    private Context buildContext(Notification notification) {
        Context context = new Context();
        context.setVariable("userFullName", notification.getRecipient().getFullName());
        context.setVariable("title", notification.getTitle());
        context.setVariable("message", notification.getMessage());
        context.setVariable("status", notification.getStatus());

        // Enrich context for LeaveRequest notifications
        if (notification.getType() == NotificationType.LEAVE && notification.getEntityId() != null) {
            Optional<LeaveRequest> leaveOpt = leaveRequestRepository.findById(notification.getEntityId());
            leaveOpt.ifPresent(leave -> {
                context.setVariable("period", formatPeriod(leave.getStartDate(), leave.getEndDate()));
                context.setVariable("leaveType", leave.getType());
                context.setVariable("notes", leave.getNotes());
                context.setVariable("approverFullName", leave.getApprover() != null ? leave.getApprover().getFullName() : null);
                context.setVariable("approvedAt", leave.getApprovedAt());
                context.setVariable("organizationName", leave.getOrganization().getName());
            });
        }

        // Enrich context for AttendanceRecord notifications
        if (notification.getType() == NotificationType.ATTENDANCE && notification.getEntityId() != null) {
            Optional<AttendanceRecord> attOpt = attendanceRecordRepository.findById(notification.getEntityId());
            attOpt.ifPresent(att -> {
                context.setVariable("date", att.getDate());
                context.setVariable("startTime", att.getStartTime());
                context.setVariable("endTime", att.getEndTime());
                context.setVariable("attendanceType", att.getType());
                context.setVariable("notes", att.getNotes());
                context.setVariable("approverFullName", att.getApprover() != null ? att.getApprover().getFullName() : null);
                context.setVariable("approvedAt", att.getApprovedAt());
                context.setVariable("organizationName", att.getOrganization().getName());
            });
        }

        return context;
    }

    private String formatPeriod(java.time.LocalDate start, java.time.LocalDate end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return fmt.format(start) + " - " + fmt.format(end);
    }
}