package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.Team;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TeamSeeder {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;

    public void seedIfTableEmpty() {
        if (teamRepository.count() == 0) {
            Optional<Organization> cloudCore = organizationRepository.findByName("CloudCore");
            Optional<Organization> acmeLtd = organizationRepository.findByName("Acme Ltd");
            Optional<Organization> globalTech = organizationRepository.findByName("GlobalTech");

            cloudCore.ifPresent(org -> {
                teamRepository.save(Team.builder().name("Development").description("Backend and Frontend Development Team").organization(org).build());
                teamRepository.save(Team.builder().name("QA").description("Quality Assurance Team").organization(org).build());
                teamRepository.save(Team.builder().name("HR").description("Human Resources Team").organization(org).build());
            });
            acmeLtd.ifPresent(org -> {
                teamRepository.save(Team.builder().name("Product").description("Product Design & Delivery Team").organization(org).build());
                teamRepository.save(Team.builder().name("Support").description("Customer Support Team").organization(org).build());
            });
            globalTech.ifPresent(org -> {
                teamRepository.save(Team.builder().name("Operations").description("Operations and Logistics Team").organization(org).build());
                teamRepository.save(Team.builder().name("Marketing").description("Marketing & Communications Team").organization(org).build());
            });
        }
    }
}