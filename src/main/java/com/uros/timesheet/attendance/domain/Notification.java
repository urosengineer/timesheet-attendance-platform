package com.uros.timesheet.attendance.domain;

import com.uros.timesheet.attendance.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2048)
    private String message;

    @Column(nullable = false)
    private String status; // SENT, FAILED, PENDING

    private Instant sentAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_type")
    private String entityType;
}