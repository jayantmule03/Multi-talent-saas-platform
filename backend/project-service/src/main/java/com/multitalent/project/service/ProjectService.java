package com.multitalent.project.service;

import com.multitalent.common.event.ProjectCreatedEvent;
import com.multitalent.common.exception.ResourceNotFoundException;
import com.multitalent.common.exception.TenantAccessException;
import com.multitalent.common.kafka.EventProducer;
import com.multitalent.common.security.AuthenticatedPrincipal;
import com.multitalent.project.dto.ProjectRequest;
import com.multitalent.project.dto.ProjectResponse;
import com.multitalent.project.entity.Project;
import com.multitalent.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EventProducer eventProducer;

    @Transactional
    @CacheEvict(value = "projects", key = "#principal.tenantId")
    public ProjectResponse createProject(ProjectRequest request, AuthenticatedPrincipal principal) {
        Project project = Project.builder()
                .tenantId(principal.getTenantId())
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(principal.getUserId())
                .build();

        Project saved = projectRepository.save(project);

        eventProducer.publishAuditEvent(ProjectCreatedEvent.builder()
                .eventType("PROJECT_CREATED").tenantId(saved.getTenantId()).occurredAt(Instant.now())
                .projectId(saved.getId()).projectName(saved.getName())
                .createdByUserId(principal.getUserId()).createdByEmail(principal.getEmail())
                .build());
        eventProducer.publishAnalyticsEvent(ProjectCreatedEvent.builder()
                .eventType("PROJECT_CREATED").tenantId(saved.getTenantId()).occurredAt(Instant.now())
                .projectId(saved.getId()).projectName(saved.getName())
                .createdByUserId(principal.getUserId()).createdByEmail(principal.getEmail())
                .build());

        return ProjectResponse.fromEntity(saved);
    }

    @Cacheable(value = "projects", key = "#tenantId")
    public List<ProjectResponse> getProjectsForTenant(String tenantId) {
        return projectRepository.findByTenantId(tenantId).stream()
                .map(ProjectResponse::fromEntity)
                .toList();
    }

    public ProjectResponse getProject(String id, String tenantId) {
        Project project = projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#tenantId")
    public ProjectResponse updateProject(String id, String tenantId, ProjectRequest request) {
        Project project = projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return ProjectResponse.fromEntity(projectRepository.save(project));
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#tenantId")
    public void deleteProject(String id, String tenantId) {
        Project project = projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        if (!project.getTenantId().equals(tenantId)) {
            throw new TenantAccessException("Not authorized to delete this project");
        }
        projectRepository.delete(project);
    }
}
