package com.project_management.services;

import com.project_management.dto.SubTaskDTO;
import java.util.List;

public interface SubTaskService {
    SubTaskDTO createSubTask(SubTaskDTO subTaskDTO);
    SubTaskDTO getSubTaskById(Long id);
    List<SubTaskDTO> getAllSubTasks();
    SubTaskDTO updateSubTask(Long id, SubTaskDTO subTaskDTO);
    void deleteSubTask(Long id);
    SubTaskDTO assignSubTaskToUser(Long subTaskId, Long userId);
}
