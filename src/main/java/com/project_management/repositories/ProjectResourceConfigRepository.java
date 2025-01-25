package com.project_management.repositories;

import com.project_management.models.ProjectResourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectResourceConfigRepository extends JpaRepository<ProjectResourceConfig,Long> {
    ProjectResourceConfig findByProjectId(Long id);
}
