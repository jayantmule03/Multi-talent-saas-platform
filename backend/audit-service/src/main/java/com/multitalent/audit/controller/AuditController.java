package com.multitalent.audit.controller;

import com.multitalent.audit.document.AuditLog;
import com.multitalent.audit.service.AuditService;
import com.multitalent.common.response.ApiResponse;
import com.multitalent.common.security.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only API so the frontend (or an admin dashboard) can pull the audit
 * trail for the caller's tenant. Protected by the shared JwtAuthFilter.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getMyTenantAuditLog(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getLogsForTenant(principal.getTenantId())));
    }
}
