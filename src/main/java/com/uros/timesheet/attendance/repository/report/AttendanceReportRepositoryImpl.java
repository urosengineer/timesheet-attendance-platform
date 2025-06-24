package com.uros.timesheet.attendance.repository.report;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Repository
public class AttendanceReportRepositoryImpl implements AttendanceReportRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public List<UserAttendanceSummaryDto> getUserAttendanceSummary(UUID userId, LocalDate from, LocalDate to) {
        String jpql = """
            SELECT ar FROM AttendanceRecord ar
            JOIN FETCH ar.user u
            WHERE u.id = :userId
              AND ar.date >= :fromDate
              AND ar.date <= :toDate
              AND ar.status = 'APPROVED'
        """;

        List<AttendanceRecord> records = em.createQuery(jpql, AttendanceRecord.class)
                .setParameter("userId", userId)
                .setParameter("fromDate", from)
                .setParameter("toDate", to)
                .getResultList();

        if (records.isEmpty()) {
            return List.of();
        }

        AttendanceRecord first = records.get(0);
        long totalDays = records.stream().map(AttendanceRecord::getDate).distinct().count();
        long totalRecords = records.size();
        double totalHours = records.stream()
                .mapToDouble(r -> {
                    if (r.getStartTime() == null || r.getEndTime() == null) return 0.0;
                    return Duration.between(r.getStartTime(), r.getEndTime()).toMinutes() / 60.0;
                })
                .sum();

        UserAttendanceSummaryDto summary = new UserAttendanceSummaryDto(
                first.getUser().getId(),
                first.getUser().getFullName(),
                from,
                to,
                totalDays,
                totalRecords,
                BigDecimal.valueOf(totalHours)
        );

        return List.of(summary);
    }
}