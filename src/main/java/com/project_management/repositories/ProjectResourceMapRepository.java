package com.project_management.repositories;

import com.project_management.models.ProjectResourceMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectResourceMapRepository extends JpaRepository<ProjectResourceMap, Long> {
    List<ProjectResourceMap> findByProjectId(Long projectId);
}
