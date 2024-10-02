package com.project_management.servicesImpl;

import com.project_management.dto.SubTaskDTO;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.repositories.SubTaskRepository;
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

    @Override
    public SubTaskDTO createSubTask(SubTaskDTO subTaskDTO) {
        SubTask subTask = new SubTask();
        BeanUtils.copyProperties(subTaskDTO, subTask);
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
        BeanUtils.copyProperties(subTaskDTO, existingSubTask);
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
        SubTaskDTO subTaskDTO = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, subTaskDTO);
        subTaskDTO.setAssignedUserId(subTask.getAssignedUser() != null ? subTask.getAssignedUser().getId() : null);
        return subTaskDTO;
    }
}
