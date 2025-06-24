package com.uros.timesheet.attendance.service.helper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMetricHelper {

    private final MeterRegistry meterRegistry;

    public Timer.Sample startCreateUserTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCreateUserTimer(Timer.Sample sample, boolean success) {
        if (sample != null) {
            sample.stop(meterRegistry.timer("user.create.latency", "status", success ? "SUCCESS" : "FAIL"));
        }
        meterRegistry.counter("user.create.count", "result", success ? "SUCCESS" : "FAIL").increment();
    }

    public Timer.Sample startSoftDeleteUserTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopSoftDeleteUserTimer(Timer.Sample sample, boolean success) {
        if (sample != null) {
            sample.stop(meterRegistry.timer("user.softdelete.latency", "status", success ? "SUCCESS" : "FAIL"));
        }
        meterRegistry.counter("user.softdelete.count", "result", success ? "SUCCESS" : "FAIL").increment();
    }

    public Timer.Sample startRestoreUserTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopRestoreUserTimer(Timer.Sample sample, boolean success) {
        if (sample != null) {
            sample.stop(meterRegistry.timer("user.restore.latency", "status", success ? "SUCCESS" : "FAIL"));
        }
        meterRegistry.counter("user.restore.count", "result", success ? "SUCCESS" : "FAIL").increment();
    }
}