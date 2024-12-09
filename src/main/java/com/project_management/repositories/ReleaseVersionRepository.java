package com.project_management.repositories;

import com.project_management.models.ReleaseVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long> {
    List<ReleaseVersion> findByProjectId(Long projectId);
}
