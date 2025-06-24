package com.uros.timesheet.attendance.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String type; // work, leave, sick, remote

    @Column(nullable = false)
    private String status; // DRAFT, SUBMITTED, APPROVED, REJECTED, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    private Instant approvedAt;

    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() { return deletedAt != null; }
    public void markDeleted() { this.deletedAt = Instant.now(); this.status = "DELETED"; }
    public void restore() { this.deletedAt = null; if ("DELETED".equals(this.status)) this.status = "DRAFT"; }
}