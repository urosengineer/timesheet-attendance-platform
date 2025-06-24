package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.auditlog.AuditLog;
import com.uros.timesheet.attendance.auditlog.AuditLogRepository;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogSeeder {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void seedIfTableEmpty() {
        if (auditLogRepository.count() == 0) {
            Optional<User> urosOpt = userRepository.findByUsername("uros");
            Optional<User> alisonOpt = userRepository.findByUsername("alison.carter");
            Optional<User> michaelOpt = userRepository.findByUsername("michael.evans");

            auditLogRepository.save(AuditLog.builder()
                    .eventType("SYSTEM_BOOTSTRAP")
                    .user(null)
                    .details("Initial system bootstrap and database seed complete.")
                    .createdAt(Instant.now())
                    .ipAddress("127.0.0.1")
                    .userAgent("SeederBot/1.0")
                    .build());

            urosOpt.ifPresent(uros -> auditLogRepository.save(AuditLog.builder()
                    .eventType("LOGIN")
                    .user(uros)
                    .details("{\"username\":\"uros\",\"status\":\"SUCCESS\"}")
                    .createdAt(Instant.now().minusSeconds(18000))
                    .ipAddress("192.168.1.10")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()));

            michaelOpt.ifPresent(michael -> auditLogRepository.save(AuditLog.builder()
                    .eventType("CREATE_LEAVE_REQUEST")
                    .user(michael)
                    .details("{\"leaveType\":\"annual\",\"from\":\"2025-06-24\",\"to\":\"2025-06-29\"}")
                    .createdAt(Instant.now().minusSeconds(17200))
                    .ipAddress("192.168.1.22")
                    .userAgent("Chrome/120.0.0.0")
                    .build()));

            alisonOpt.ifPresent(alison -> auditLogRepository.save(AuditLog.builder()
                    .eventType("APPROVE_LEAVE_REQUEST")
                    .user(alison)
                    .details("{\"requestId\":\"demo-uuid-1234\",\"status\":\"APPROVED\"}")
                    .createdAt(Instant.now().minusSeconds(16800))
                    .ipAddress("192.168.1.25")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 13_3)")
                    .build()));

            urosOpt.ifPresent(uros -> auditLogRepository.save(AuditLog.builder()
                    .eventType("UPDATE_ATTENDANCE")
                    .user(uros)
                    .details("{\"recordId\":\"demo-att-5555\",\"status\":\"APPROVED\",\"changedBy\":\"uros\"}")
                    .createdAt(Instant.now().minusSeconds(12000))
                    .ipAddress("192.168.1.10")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()));
        }
    }
}