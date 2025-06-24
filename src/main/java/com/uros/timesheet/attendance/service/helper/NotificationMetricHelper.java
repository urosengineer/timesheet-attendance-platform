package com.uros.timesheet.attendance.service.helper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetricHelper {

    private final MeterRegistry meterRegistry;

    public NotificationMetricHelper(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startSample() {
        return Timer.start(meterRegistry);
    }

    public void incrementTotal(String type) {
        meterRegistry.counter("notifications.total", "type", type != null ? type : "UNKNOWN").increment();
    }

    public void incrementStatus(String type, boolean sentSuccessfully) {
        meterRegistry.counter(
                "notifications.status",
                "type", type != null ? type : "UNKNOWN",
                "status", sentSuccessfully ? "SENT" : "FAILED"
        ).increment();
    }

    public void recordLatency(String type, boolean sentSuccessfully, Timer.Sample sample) {
        if ("EMAIL".equals(type)) {
            sample.stop(
                    meterRegistry.timer("notifications.email.latency", "type", type, "status", sentSuccessfully ? "SENT" : "FAILED")
            );
        }
    }
}