package com.multitalent.tenant.service;

import com.multitalent.common.exception.DuplicateResourceException;
import com.multitalent.common.exception.ResourceNotFoundException;
import com.multitalent.tenant.dto.TenantRequest;
import com.multitalent.tenant.dto.TenantResponse;
import com.multitalent.tenant.entity.Tenant;
import com.multitalent.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public TenantResponse createTenant(TenantRequest request) {
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Tenant slug already in use: " + request.getSlug());
        }
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();
        return TenantResponse.fromEntity(tenantRepository.save(tenant));
    }

    @Cacheable(value = "tenants", key = "#slug")
    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + slug));
        return TenantResponse.fromEntity(tenant);
    }

    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(TenantResponse::fromEntity)
                .toList();
    }
}
