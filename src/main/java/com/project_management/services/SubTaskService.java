package com.project_management.services;

import com.project_management.dto.SubTaskDTO;
import com.project_management.dto.SubTaskDTONew;

import java.util.List;

public interface SubTaskService {
    SubTaskDTO createSubTask(SubTaskDTO subTaskDTO);
    SubTaskDTO getSubTaskById(Long id);
    List<SubTaskDTO> getAllSubTasks();
    List<SubTaskDTO> getAllSubTasksByTaskId(long taskId);
    SubTaskDTO updateSubTask(Long id, SubTaskDTO subTaskDTO);
    void deleteSubTask(Long id);
    SubTaskDTO assignSubTaskToUser(Long subTaskId, Long userId);
    List<SubTaskDTONew> getSubTasksByTaskId(Long taskId);
    List<SubTaskDTONew> getSubTasksByAssignedUserId(Long userId);
}
