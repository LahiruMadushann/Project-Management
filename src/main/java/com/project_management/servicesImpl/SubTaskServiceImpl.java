package com.project_management.servicesImpl;

import com.project_management.dto.SubTaskDTO;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.repositories.SubTaskRepository;
import com.project_management.repositories.TaskRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.SubTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public SubTaskDTO createSubTask(SubTaskDTO subTaskDTO) {
        Task parentTask = taskRepository.findById(subTaskDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("Parent Task not found"));

        SubTask subTask = new SubTask();
        BeanUtils.copyProperties(subTaskDTO, subTask, "id", "task");

        subTask.setTask(parentTask);
        subTask.setCreatedAt(LocalDateTime.now());

        SubTask savedSubTask = subTaskRepository.save(subTask);
        return convertToDTO(savedSubTask);
    }

    @Override
    public SubTaskDTO getSubTaskById(Long id) {
        SubTask subTask = subTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));
        return convertToDTO(subTask);
    }

    @Override
    public List<SubTaskDTO> getAllSubTasks() {
        return subTaskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubTaskDTO updateSubTask(Long id, SubTaskDTO subTaskDTO) {
        SubTask existingSubTask = subTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));

        Task existingTask = existingSubTask.getTask();
        Long existingCreateUserId = existingSubTask.getCreateUserId();

        String[] ignoredProperties = {"id", "task", "createUserId", "createdAt"};

        BeanUtils.copyProperties(subTaskDTO, existingSubTask, ignoredProperties);

        if (subTaskDTO.getTaskId() != null &&
                !subTaskDTO.getTaskId().equals(existingTask.getId())) {
            Task newTask = taskRepository.findById(subTaskDTO.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));
            existingSubTask.setTask(newTask);
        } else {
            existingSubTask.setTask(existingTask);
        }

        existingSubTask.setCreateUserId(existingCreateUserId);

        existingSubTask.setUpdatedAt(LocalDateTime.now());

        SubTask updatedSubTask = subTaskRepository.save(existingSubTask);
        return convertToDTO(updatedSubTask);
    }

    @Override
    public void deleteSubTask(Long id) {
        subTaskRepository.deleteById(id);

    }

    @Override
    public SubTaskDTO assignSubTaskToUser(Long subTaskId, Long userId) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        subTask.setAssignedUser(user);
        subTask.setAssignedDate(LocalDate.now());
        subTask.setUpdatedAt(LocalDateTime.now());

        SubTask updatedSubTask = subTaskRepository.save(subTask);
        return convertToDTO(updatedSubTask);
    }

    private SubTaskDTO convertToDTO(SubTask subTask) {
        SubTaskDTO dto = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, dto);
        dto.setTaskId(subTask.getTask().getId());
        return dto;
    }
}
