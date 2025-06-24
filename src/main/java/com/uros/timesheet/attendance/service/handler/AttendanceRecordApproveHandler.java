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
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.workflow.WorkflowEngineService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.exception.WorkflowTransitionDeniedException;


import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttendanceRecordApproveHandler {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final WorkflowEngineService workflowEngineService;
    private final MessageUtil messageUtil;
    private final DomainEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public AttendanceRecordResponse handle(UUID id, UUID approverId) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        boolean success = false;
        String status = null;
        try {
            AttendanceRecord record = attendanceRecordRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.attendance.not.found")));
            if (record.getUser().getId().equals(approverId)) {
                throw new IllegalArgumentException(messageUtil.get("error.attendance.approve.self"));
            }

            User approver = userRepository.findById(approverId)
                    .orElseThrow(() -> new NotFoundException("error.user.not.found"));
            Set<String> approverRoles = approver.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

            if (!workflowEngineService.canTransition("AttendanceRecord", record.getStatus(), "APPROVED", approverRoles)) {
                throw new WorkflowTransitionDeniedException("error.workflow.transition.denied");
            }

            String oldStatus = record.getStatus();
            status = "APPROVED";
            record.setStatus(status);
            record.setApprover(approver);
            record.setApprovedAt(Instant.now());
            record.setUpdatedAt(Instant.now());
            attendanceRecordRepository.save(record);

            eventPublisher.publish(new AttendanceRecordStatusChangedEvent(
                    this,
                    record.getId(),
                    record.getUser().getId(),
                    oldStatus,
                    status,
                    approverId,
                    null,
                    record.getUpdatedAt()
            ));

            success = true;
            return attendanceRecordMapper.toResponse(record);
        } finally {
            timerSample.stop(meterRegistry.timer("attendance.approve.latency", "status", success ? "SUCCESS" : "FAIL"));
            meterRegistry.counter(
                    "attendance.approve.count",
                    "status", status != null ? status : "UNKNOWN",
                    "result", success ? "SUCCESS" : "FAIL"
            ).increment();
        }
    }
}
