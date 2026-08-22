package com.multitalent.project.repository;

import com.multitalent.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByTenantId(String tenantId);
    Optional<Project> findByIdAndTenantId(String id, String tenantId);
}
