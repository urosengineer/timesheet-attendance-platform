package com.uros.timesheet.attendance.graphql;

import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserGraphQLController {

    private final UserService userService;

    @QueryMapping
    public UserResponse user(@Argument UUID id) {
        return userService.getUserById(id);
    }

    @QueryMapping
    public List<UserResponse> users() {
        return userService.getUsersForCurrentTenant();
    }
}