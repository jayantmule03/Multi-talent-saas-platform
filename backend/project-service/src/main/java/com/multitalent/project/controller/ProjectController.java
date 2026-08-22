package com.multitalent.project.controller;

import com.multitalent.common.response.ApiResponse;
import com.multitalent.common.security.AuthenticatedPrincipal;
import com.multitalent.project.dto.ProjectRequest;
import com.multitalent.project.dto.ProjectResponse;
import com.multitalent.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectRequest request,
                                                                 @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        ProjectResponse response = projectService.createProject(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Project created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAll(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectsForTenant(principal.getTenantId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getOne(@PathVariable String id,
                                                                 @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(id, principal.getTenantId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable String id,
                                                                  @Valid @RequestBody ProjectRequest request,
                                                                  @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        ProjectResponse response = projectService.updateProject(id, principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.success("Project updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id,
                                                       @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        projectService.deleteProject(id, principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }
}
