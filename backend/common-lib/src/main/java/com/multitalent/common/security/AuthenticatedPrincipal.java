package com.multitalent.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Lightweight principal built directly from JWT claims. Used by resource
 * services (tenant-service, project-service) that need "who is calling and
 * what tenant/role are they" without owning the users table themselves.
 */
@Data
@AllArgsConstructor
public class AuthenticatedPrincipal {
    private String userId;
    private String email;
    private String tenantId;
    private String role;
}
