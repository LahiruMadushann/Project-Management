package com.project_management.repositories;

import com.project_management.dto.SubTaskDTONew;
import com.project_management.models.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findByTaskId(Long taskId);
    List<SubTask> findByAssignedUserId(Long userId);
}
