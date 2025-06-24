package com.uros.timesheet.attendance.util;

import com.uros.timesheet.attendance.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring filter that populates the TenantContext for each request
 * based on the authenticated user's organization ID.
 * Ensures proper isolation and cleanup of tenant information per thread.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                if (userDetails.getOrganizationId() != null) {
                    TenantContext.setTenantId(userDetails.getOrganizationId().toString());
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the tenant context to avoid memory leaks (ThreadLocal cleanup)
            TenantContext.clear();
        }
    }
}