package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationSeeder {

    private final OrganizationRepository organizationRepository;

    public void seedIfTableEmpty() {
        if (organizationRepository.count() == 0) {
            organizationRepository.save(
                    Organization.builder()
                            .name("CloudCore")
                            .status("ACTIVE")
                            .timezone("Europe/Belgrade")
                            .build());
            organizationRepository.save(
                    Organization.builder()
                            .name("Acme Ltd")
                            .status("ACTIVE")
                            .timezone("Europe/London")
                            .build());
            organizationRepository.save(
                    Organization.builder()
                            .name("GlobalTech")
                            .status("ACTIVE")
                            .timezone("America/New_York")
                            .build());
        }
    }
}