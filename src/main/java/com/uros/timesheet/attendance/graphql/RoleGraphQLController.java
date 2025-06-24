package com.uros.timesheet.attendance.graphql;

import com.uros.timesheet.attendance.dto.role.RoleResponse;
import com.uros.timesheet.attendance.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RoleGraphQLController {

    private final RoleService roleService;

    @QueryMapping
    public RoleResponse role(@Argument UUID id) {
        return roleService.getRoleById(id);
    }

    @QueryMapping
    public List<RoleResponse> roles() {
        return roleService.getAllRoles();
    }
}