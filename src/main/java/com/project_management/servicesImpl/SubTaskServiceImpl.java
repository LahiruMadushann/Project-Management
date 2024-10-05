package com.project_management.servicesImpl;

import com.project_management.dto.SubTaskDTO;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.models.enums.TaskStatus;
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
        if (subTaskDTO.getAssignedUserId() != null) {
            User user = userRepository.findById(subTaskDTO.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            subTask.setAssignedUser(user);
        }

        subTask.setTask(parentTask);
        subTask.setCreatedAt(LocalDateTime.now());
        subTask.setUpdatedAt(LocalDateTime.now());
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

        if (subTaskDTO.getName() != null) {
            existingSubTask.setName(subTaskDTO.getName());
        }

        if (subTaskDTO.getStatus() != null) {
            existingSubTask.setStatus(subTaskDTO.getStatus());
        }

        if (subTaskDTO.getTags() != null) {
            existingSubTask.setTags(subTaskDTO.getTags());
        }

        if (subTaskDTO.getAssignedDate() != null) {
            existingSubTask.setAssignedDate(subTaskDTO.getAssignedDate());
        }

        if (subTaskDTO.getStartDate() != null) {
            existingSubTask.setStartDate(subTaskDTO.getStartDate());
        }

        if (subTaskDTO.getDeadline() != null) {
            existingSubTask.setDeadline(subTaskDTO.getDeadline());
        }

        if (subTaskDTO.getCompletedDate() != null) {
            existingSubTask.setCompletedDate(subTaskDTO.getCompletedDate());
        }

        if (subTaskDTO.getTaskId() != null &&
                !subTaskDTO.getTaskId().equals(existingSubTask.getTask().getId())) {
            Task newTask = taskRepository.findById(subTaskDTO.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));
            existingSubTask.setTask(newTask);
        }

        if (subTaskDTO.getAssignedUserId() != null &&
                (existingSubTask.getAssignedUser() == null || !subTaskDTO.getAssignedUserId().equals(existingSubTask.getAssignedUser().getId()))) {
            User newAssignedUser = userRepository.findById(subTaskDTO.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned User not found"));
            existingSubTask.setAssignedUser(newAssignedUser);
        }

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
        SubTaskDTO subTaskDto = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, subTaskDto);
        subTaskDto.setTaskId(subTask.getTask().getId());
        if (subTask.getAssignedUser() != null && subTask.getAssignedUser().getId() != null){
            subTaskDto.setAssignedUserId(subTask.getAssignedUser().getId());
        }
        return subTaskDto;
    }
}
