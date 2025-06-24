package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.event.AttendanceRecordStatusChangedEvent;
import com.uros.timesheet.attendance.event.DomainEventPublisher;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.AttendanceRecordMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttendanceRecordRestoreHandler {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final DomainEventPublisher eventPublisher;
    private final MessageUtil messageUtil;
    private final MeterRegistry meterRegistry;

    public AttendanceRecordResponse handle(UUID id, UUID performedByUserId, String reason) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        boolean success = false;
        String oldStatus = null;
        try {
            AttendanceRecord record = attendanceRecordRepository.findByIdIncludingDeleted(id)
                    .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.attendance.not.found")));
            if (!record.isDeleted()) {
                throw new IllegalStateException(messageUtil.get("error.attendance.not.deleted"));
            }
            oldStatus = record.getStatus();
            record.restore();
            record.setUpdatedAt(Instant.now());
            attendanceRecordRepository.save(record);

            eventPublisher.publish(new AttendanceRecordStatusChangedEvent(
                    this,
                    record.getId(),
                    record.getUser().getId(),
                    "DELETED",
                    oldStatus,
                    performedByUserId,
                    reason,
                    record.getUpdatedAt()
            ));

            success = true;
            return attendanceRecordMapper.toResponse(record);
        } finally {
            timerSample.stop(meterRegistry.timer("attendance.restore.latency", "status", success ? "SUCCESS" : "FAIL"));
            meterRegistry.counter(
                    "attendance.restore.count",
                    "status", oldStatus != null ? oldStatus : "UNKNOWN",
                    "result", success ? "SUCCESS" : "FAIL"
            ).increment();
        }
    }
}