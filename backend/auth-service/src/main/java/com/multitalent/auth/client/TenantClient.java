package com.multitalent.auth.client;

import com.multitalent.common.exception.ResourceNotFoundException;
import com.multitalent.common.exception.UpstreamServiceException;
import com.multitalent.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Thin REST client auth-service uses to call tenant-service directly
 * (service-to-service, over the network — this is a real microservices
 * call, not a shared-code shortcut).
 */
@Slf4j
@Component
public class TenantClient {

    private final RestClient restClient;

    public TenantClient(@Value("${services.tenant-service.base-url}") String tenantServiceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(tenantServiceBaseUrl).build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTenantBySlug(String slug) {
        try {
            ApiResponse<Map> response = restClient.get()
                    .uri("/api/tenants/{slug}", slug)
                    .retrieve()
                    .body(ApiResponse.class);

            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Tenant not found: " + slug);
            }
            return (Map<String, Object>) response.getData();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatusCode.valueOf(404)) {
                throw new ResourceNotFoundException("Tenant not found: " + slug);
            }
            log.error("tenant-service call failed for slug {}", slug, ex);
            throw new UpstreamServiceException("Failed to reach tenant-service: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("tenant-service call failed for slug {}", slug, ex);
            throw new UpstreamServiceException("Failed to reach tenant-service: " + ex.getMessage(), ex);
        }
    }
}
