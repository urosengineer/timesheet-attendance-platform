package com.uros.timesheet.attendance.security;

import com.uros.timesheet.attendance.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String fullName;
    private final String password;
    private final String status;
    private final UUID organizationId;
    private final String organizationName;
    private final Set<String> roleNames;
    private final Set<String> permissionNames;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.status = user.getStatus();
        this.organizationId = user.getOrganization().getId();
        this.organizationName = user.getOrganization().getName();

        // Role names as strings
        this.roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        // Permission names as strings
        this.permissionNames = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        this.password = user.getPasswordHash();

        // Combine both permissions and roles (with ROLE_ prefix) as authorities
        Set<GrantedAuthority> authorities = new HashSet<>();
        // permissions
        authorities.addAll(user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(perm -> new SimpleGrantedAuthority(perm.getName()))
                .collect(Collectors.toSet()));
        // roles (Spring expects "ROLE_" prefix)
        authorities.addAll(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet()));
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return !"SUSPENDED".equals(status); }
    @Override
    public boolean isAccountNonLocked() { return !"SUSPENDED".equals(status); }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return "ACTIVE".equals(status); }
}