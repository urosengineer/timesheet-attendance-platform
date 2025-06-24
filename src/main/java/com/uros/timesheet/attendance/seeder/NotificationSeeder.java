package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.enums.NotificationType;
import com.uros.timesheet.attendance.repository.NotificationRepository;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationSeeder {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public void seedIfTableEmpty() {
        if (notificationRepository.count() == 0) {
            Optional<User> urosOpt = userRepository.findByUsername("uros");
            Optional<User> alisonOpt = userRepository.findByUsername("alison.carter");
            Optional<User> michaelOpt = userRepository.findByUsername("michael.evans");
            Optional<User> liamOpt = userRepository.findByUsername("liam.martin");
            Optional<User> oliviaOpt = userRepository.findByUsername("olivia.jones");

            Organization cloudCore = organizationRepository.findByName("CloudCore").orElse(null);
            Organization acmeLtd = organizationRepository.findByName("Acme Ltd").orElse(null);

            urosOpt.ifPresent(uros -> notificationRepository.save(Notification.builder()
                    .recipient(uros)
                    .type(NotificationType.EMAIL)
                    .title("Welcome to CloudCore Platform")
                    .message("Dear Uros, welcome to your new CloudCore platform. Feel free to reach out for any help.")
                    .status("SENT")
                    .sentAt(Instant.now())
                    .createdAt(Instant.now())
                    .entityId(uros.getId())
                    .entityType("User")
                    .build()));

            if (alisonOpt.isPresent() && michaelOpt.isPresent()) {
                User alison = alisonOpt.get();
                User michael = michaelOpt.get();
                notificationRepository.save(Notification.builder()
                        .recipient(alison)
                        .type(NotificationType.WEBSOCKET)
                        .title("Leave request pending approval")
                        .message("Michael Evans has submitted a leave request for your review.")
                        .status("SENT")
                        .sentAt(Instant.now())
                        .createdAt(Instant.now())
                        .entityId(michael.getId())
                        .entityType("LeaveRequest")
                        .build());
            }

            liamOpt.ifPresent(liam -> notificationRepository.save(Notification.builder()
                    .recipient(liam)
                    .type(NotificationType.DUMMY)
                    .title("Attendance Reminder")
                    .message("Don't forget to submit your attendance for this week.")
                    .status("SENT")
                    .sentAt(Instant.now())
                    .createdAt(Instant.now())
                    .entityId(liam.getId())
                    .entityType("AttendanceRecord")
                    .build()));

            oliviaOpt.ifPresent(olivia -> notificationRepository.save(Notification.builder()
                    .recipient(olivia)
                    .type(NotificationType.EMAIL)
                    .title("HR System Sync Failed")
                    .message("We were unable to sync your profile with the HR system. Please contact support.")
                    .status("FAILED")
                    .createdAt(Instant.now())
                    .entityId(olivia.getId())
                    .entityType("User")
                    .build()));

            if (cloudCore != null) {
                userRepository.findAll().stream()
                        .filter(u -> u.getOrganization().getId().equals(cloudCore.getId()))
                        .forEach(user -> notificationRepository.save(Notification.builder()
                                .recipient(user)
                                .type(NotificationType.WEBSOCKET)
                                .title("CloudCore All-Hands Meeting")
                                .message("The monthly all-hands meeting is scheduled for Friday at 10:00 AM.")
                                .status("SENT")
                                .sentAt(Instant.now())
                                .createdAt(Instant.now())
                                .entityType("Organization")
                                .entityId(cloudCore.getId())
                                .build()));
            }
        }
    }
}