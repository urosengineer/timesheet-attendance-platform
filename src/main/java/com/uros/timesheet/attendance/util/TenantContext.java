package com.uros.timesheet.attendance.util;

/**
 * Utility class for managing the current tenant (organization) context per thread.
 * In real-world multi-tenant applications, this is typically set from a JWT, HTTP header,
 * or Spring Security context.
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Set the tenant (organization) ID for the current thread.
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Retrieve the current tenant (organization) ID for this thread.
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clear the tenant context for the current thread to prevent memory leaks.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}