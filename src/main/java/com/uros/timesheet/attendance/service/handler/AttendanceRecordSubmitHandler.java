package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.event.AttendanceRecordStatusChangedEvent;
import com.uros.timesheet.attendance.event.DomainEventPublisher;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.AttendanceRecordMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.service.helper.AttendanceMetricHelper;
import com.uros.timesheet.attendance.workflow.WorkflowEngineService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.uros.timesheet.attendance.exception.WorkflowTransitionDeniedException;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttendanceRecordSubmitHandler {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final WorkflowEngineService workflowEngineService;
    private final MessageUtil messageUtil;
    private final DomainEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public AttendanceRecordResponse handle(UUID id, UUID currentUserId) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        boolean success = false;
        String status = null;
        try {
            AttendanceRecord record = attendanceRecordRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.attendance.not.found")));
            if (!record.getUser().getId().equals(currentUserId)) {
                throw new IllegalArgumentException(messageUtil.get("error.attendance.unauthorized"));
            }

            User user = record.getUser();
            Set<String> userRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

            if (!workflowEngineService.canTransition("AttendanceRecord", record.getStatus(), "SUBMITTED", userRoles)) {
                throw new WorkflowTransitionDeniedException("error.workflow.transition.denied");
            }

            String oldStatus = record.getStatus();
            status = "SUBMITTED";
            record.setStatus(status);
            record.setUpdatedAt(Instant.now());
            attendanceRecordRepository.save(record);

            eventPublisher.publish(new AttendanceRecordStatusChangedEvent(
                    this,
                    record.getId(),
                    record.getUser().getId(),
                    oldStatus,
                    status,
                    currentUserId,
                    null,
                    record.getUpdatedAt()
            ));

            success = true;
            return attendanceRecordMapper.toResponse(record);
        } finally {
            timerSample.stop(meterRegistry.timer("attendance.submit.latency", "status", success ? "SUCCESS" : "FAIL"));
            meterRegistry.counter(
                    "attendance.submit.count",
                    "status", status != null ? status : "UNKNOWN",
                    "result", success ? "SUCCESS" : "FAIL"
            ).increment();
        }
    }
}