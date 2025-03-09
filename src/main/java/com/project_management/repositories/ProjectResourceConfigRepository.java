package com.project_management.repositories;

import com.project_management.models.ProjectResourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectResourceConfigRepository extends JpaRepository<ProjectResourceConfig,Long> {
    void deleteByProjectId(Long id);
    List<ProjectResourceConfig> findAllByProjectId(Long id);
    ProjectResourceConfig findTopByProjectIdOrderByIdDesc(Long id);
}
