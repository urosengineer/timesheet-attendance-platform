package com.uros.timesheet.attendance.service.helper;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMetricHelper {

    private final MeterRegistry meterRegistry;

    public AttendanceMetricHelper(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void increment(String operation, String status, boolean success) {
        meterRegistry.counter(
                String.format("attendance.%s.count", operation),
                "status", status != null ? status : "UNKNOWN",
                "result", success ? "SUCCESS" : "FAIL"
        ).increment();
    }
}