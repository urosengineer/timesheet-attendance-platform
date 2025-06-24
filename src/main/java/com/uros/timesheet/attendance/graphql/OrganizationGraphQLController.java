package com.uros.timesheet.attendance.graphql;

import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import com.uros.timesheet.attendance.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OrganizationGraphQLController {

    private final OrganizationService organizationService;

    @QueryMapping
    public OrganizationResponse organization(@Argument UUID id) {
        return organizationService.getOrganizationById(id);
    }

    @QueryMapping
    public List<OrganizationResponse> organizations() {
        return organizationService.getAllOrganizations();
    }
}