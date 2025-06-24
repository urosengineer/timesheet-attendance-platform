package com.uros.timesheet.attendance.service.helper;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMetricHelper {

    private final MeterRegistry meterRegistry;

    public LeaveRequestMetricHelper(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void increment(String operation, String status, boolean success) {
        meterRegistry.counter(
                String.format("leave.%s.count", operation),
                "status", status != null ? status : "UNKNOWN",
                "result", success ? "SUCCESS" : "FAIL"
        ).increment();
    }
}