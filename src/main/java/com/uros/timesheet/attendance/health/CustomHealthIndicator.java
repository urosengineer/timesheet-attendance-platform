package com.uros.timesheet.attendance.health;

import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final MessageUtil messageUtil;

    @Override
    public Health health() {
        try {
            long userCount = userRepository.count();
            long attendanceCount = attendanceRecordRepository.count();

            boolean usersOk = userCount > 0;
            boolean attendanceOk = attendanceCount >= 0;

            if (usersOk && attendanceOk) {
                return Health.up()
                        .withDetail("users.count", userCount)
                        .withDetail("attendance.count", attendanceCount)
                        .withDetail("custom.status", messageUtil.get("health.custom.status.ok"))
                        .build();
            } else {
                return Health.down()
                        .withDetail("users.count", userCount)
                        .withDetail("attendance.count", attendanceCount)
                        .withDetail("custom.status", messageUtil.get("health.custom.status.empty"))
                        .build();
            }
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("custom.status", messageUtil.get("health.custom.status.error", ex.getMessage()))
                    .build();
        }
    }
}