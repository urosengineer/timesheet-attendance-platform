package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.*;
import com.uros.timesheet.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public void seedIfTableEmpty() {
        if (userRepository.count() == 0) {
            Organization cloudCore = organizationRepository.findByName("CloudCore").orElseThrow();
            Organization acmeLtd = organizationRepository.findByName("Acme Ltd").orElseThrow();
            Organization globalTech = organizationRepository.findByName("GlobalTech").orElseThrow();

            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Role managerRole = roleRepository.findByName("MANAGER").orElseThrow();
            Role employeeRole = roleRepository.findByName("EMPLOYEE").orElseThrow();

            Team dev = teamRepository.findByName("Development").orElse(null);
            Team qa = teamRepository.findByName("QA").orElse(null);
            Team hr = teamRepository.findByName("HR").orElse(null);
            Team product = teamRepository.findByName("Product").orElse(null);
            Team support = teamRepository.findByName("Support").orElse(null);
            Team ops = teamRepository.findByName("Operations").orElse(null);
            Team mkt = teamRepository.findByName("Marketing").orElse(null);

            userRepository.save(User.builder()
                    .username("uros")
                    .email("uros.ilic@cloudcore.com")
                    .fullName("Uros Ilic")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .status("ACTIVE")
                    .organization(cloudCore)
                    .roles(Set.of(adminRole))
                    .build());
            userRepository.save(User.builder()
                    .username("james.smith")
                    .email("james.smith@acme.com")
                    .fullName("James Smith")
                    .passwordHash(passwordEncoder.encode("acmeadmin"))
                    .status("ACTIVE")
                    .organization(acmeLtd)
                    .roles(Set.of(adminRole))
                    .team(product)
                    .build());
            userRepository.save(User.builder()
                    .username("sophia.wilson")
                    .email("sophia.wilson@globaltech.com")
                    .fullName("Sophia Wilson")
                    .passwordHash(passwordEncoder.encode("gtadmin"))
                    .status("ACTIVE")
                    .organization(globalTech)
                    .roles(Set.of(adminRole))
                    .team(ops)
                    .build());
            userRepository.save(User.builder()
                    .username("alison.carter")
                    .email("alison.carter@cloudcore.com")
                    .fullName("Alison Carter")
                    .passwordHash(passwordEncoder.encode("manager123"))
                    .status("ACTIVE")
                    .organization(cloudCore)
                    .roles(Set.of(managerRole))
                    .team(dev)
                    .build());
            userRepository.save(User.builder()
                    .username("michael.evans")
                    .email("michael.evans@cloudcore.com")
                    .fullName("Michael Evans")
                    .passwordHash(passwordEncoder.encode("employee123"))
                    .status("ACTIVE")
                    .organization(cloudCore)
                    .roles(Set.of(employeeRole))
                    .team(qa)
                    .build());
            userRepository.save(User.builder()
                    .username("lucy.taylor")
                    .email("lucy.taylor@cloudcore.com")
                    .fullName("Lucy Taylor")
                    .passwordHash(passwordEncoder.encode("employee123"))
                    .status("ACTIVE")
                    .organization(cloudCore)
                    .roles(Set.of(employeeRole))
                    .team(hr)
                    .build());
            userRepository.save(User.builder()
                    .username("olivia.jones")
                    .email("olivia.jones@acme.com")
                    .fullName("Olivia Jones")
                    .passwordHash(passwordEncoder.encode("hrmanager"))
                    .status("ACTIVE")
                    .organization(acmeLtd)
                    .roles(Set.of(managerRole))
                    .team(support)
                    .build());
            userRepository.save(User.builder()
                    .username("daniel.moore")
                    .email("daniel.moore@acme.com")
                    .fullName("Daniel Moore")
                    .passwordHash(passwordEncoder.encode("acmedeveloper"))
                    .status("ACTIVE")
                    .organization(acmeLtd)
                    .roles(Set.of(employeeRole))
                    .team(product)
                    .build());
            userRepository.save(User.builder()
                    .username("liam.martin")
                    .email("liam.martin@globaltech.com")
                    .fullName("Liam Martin")
                    .passwordHash(passwordEncoder.encode("globalemp"))
                    .status("ACTIVE")
                    .organization(globalTech)
                    .roles(Set.of(employeeRole))
                    .team(mkt)
                    .build());
        }
    }
}
