package com.project_management.services;

import com.project_management.dto.MLAnalysisResponse;
import com.project_management.dto.TaskDTO;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO);
    TaskDTO getTaskById(Long id);
    List<TaskDTO> getAllTasks();
    TaskDTO updateTask(Long id, TaskDTO taskDTO);
    void deleteTask(Long id);
    TaskDTO assignTaskToUser(Long taskId, Long userId);
//    List<TaskDTO> createTasksFromMLAnalysis(MLAnalysisResponse mlAnalysis);
List<TaskDTO> createTasksFromMLAnalysis(
        MLAnalysisResponse mlAnalysis,
        Long releaseVersionId,
        Long createUserId,
        Integer difficultyLevel,
        Long assignedUserId,
        LocalDate assignedDate,
        LocalDate startDate,
        LocalDate deadline,
        LocalDate completedDate
);
}
