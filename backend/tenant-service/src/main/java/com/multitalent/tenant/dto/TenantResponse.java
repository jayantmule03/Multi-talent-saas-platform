package com.multitalent.tenant.dto;

import com.multitalent.tenant.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse implements Serializable {

    private String id;
    private String name;
    private String slug;
    private String plan;
    private boolean active;
    private LocalDateTime createdAt;

    public static TenantResponse fromEntity(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .plan(tenant.getPlan().name())
                .active(tenant.isActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
