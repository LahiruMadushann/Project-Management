package com.project_management.repositories;

import com.project_management.models.ReleaseVersion;
import com.project_management.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByReleaseVersion(ReleaseVersion releaseVersion);
    List<Task> findByAssignedUserId(Long userId);
    List<Task> findByReleaseVersionId(Long releaseVersionId);
}