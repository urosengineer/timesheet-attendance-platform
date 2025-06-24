package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordCreateRequest;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.event.AttendanceRecordStatusChangedEvent;
import com.uros.timesheet.attendance.event.DomainEventPublisher;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.AttendanceRecordMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.util.TenantContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.uros.timesheet.attendance.exception.NotFoundException;


import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttendanceRecordCreateHandler {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final MessageUtil messageUtil;
    private final DomainEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public AttendanceRecordResponse handle(AttendanceRecordCreateRequest request) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        boolean success = false;
        String status = "DRAFT";
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new NotFoundException("error.user.not.found"));
            Organization organization;
            if (request.getOrganizationId() != null) {
                organization = organizationRepository.findById(request.getOrganizationId())
                        .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));
            } else if (user.getOrganization() != null) {
                organization = user.getOrganization();
            } else {
                String tenantId = TenantContext.getTenantId();
                if (tenantId != null) {
                    organization = organizationRepository.findById(UUID.fromString(tenantId))
                            .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));
                } else {
                    throw new IllegalArgumentException(messageUtil.get("error.organization.id.required"));
                }
            }

            AttendanceRecord record = AttendanceRecord.builder()
                    .user(user)
                    .organization(organization)
                    .date(request.getDate())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .type(request.getType())
                    .status(status)
                    .notes(request.getNotes())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            attendanceRecordRepository.save(record);

            eventPublisher.publish(new AttendanceRecordStatusChangedEvent(
                    this,
                    record.getId(),
                    user.getId(),
                    "NONE",
                    "NONE",
                    user.getId(),
                    null,
                    record.getCreatedAt()
            ));

            success = true;
            return attendanceRecordMapper.toResponse(record);
        } finally {
            timerSample.stop(meterRegistry.timer("attendance.create.latency", "status", success ? "SUCCESS" : "FAIL"));
            meterRegistry.counter(
                    "attendance.create.count",
                    "status", status,
                    "result", success ? "SUCCESS" : "FAIL"
            ).increment();
        }
    }
}
