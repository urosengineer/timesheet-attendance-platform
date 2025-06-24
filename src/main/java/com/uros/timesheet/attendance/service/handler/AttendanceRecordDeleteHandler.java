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
public class AttendanceRecordDeleteHandler {

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
            AttendanceRecord record = attendanceRecordRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.attendance.not.found")));
            if (record.isDeleted()) {
                throw new IllegalStateException(messageUtil.get("error.attendance.already.deleted"));
            }
            oldStatus = record.getStatus();
            record.markDeleted();
            record.setUpdatedAt(Instant.now());
            attendanceRecordRepository.save(record);

            eventPublisher.publish(new AttendanceRecordStatusChangedEvent(
                    this,
                    record.getId(),
                    record.getUser().getId(),
                    oldStatus,
                    "DELETED",
                    performedByUserId,
                    reason,
                    record.getUpdatedAt()
            ));

            success = true;
            return attendanceRecordMapper.toResponse(record);
        } finally {
            timerSample.stop(meterRegistry.timer("attendance.delete.latency", "status", success ? "SUCCESS" : "FAIL"));
            meterRegistry.counter(
                    "attendance.delete.count",
                    "status", oldStatus != null ? oldStatus : "UNKNOWN",
                    "result", success ? "SUCCESS" : "FAIL"
            ).increment();
        }
    }
}
