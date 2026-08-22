package com.multitalent.tenant.controller;

import com.multitalent.common.response.ApiResponse;
import com.multitalent.tenant.dto.TenantRequest;
import com.multitalent.tenant.dto.TenantResponse;
import com.multitalent.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(@Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant created successfully", response));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenant(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getTenantBySlug(slug)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants() {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getAllTenants()));
    }
}
