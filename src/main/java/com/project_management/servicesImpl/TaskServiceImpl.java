package com.project_management.servicesImpl;

import com.project_management.dto.TaskDTO;
import com.project_management.models.Project;
import com.project_management.models.ReleaseVersion;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.repositories.TaskRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(taskDTO.getReleaseVersionId())
                .orElseThrow(() -> new RuntimeException("Release Version not found"));

        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task, "id", "releaseVersion");
        task.setReleaseVersion(releaseVersion);
        task.setCreatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        return convertToDTO(savedTask);
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDTO(task);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (taskDTO.getName() != null) {
            existingTask.setName(taskDTO.getName());
        }

        if (taskDTO.getStatus() != null) {
            existingTask.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
        }

        if (taskDTO.getTags() != null) {
            existingTask.setTags(taskDTO.getTags());
        }

        if (taskDTO.getAssignedDate() != null) {
            existingTask.setAssignedDate(taskDTO.getAssignedDate());
        }

        if (taskDTO.getStartDate() != null) {
            existingTask.setStartDate(taskDTO.getStartDate());
        }

        if (taskDTO.getDeadline() != null) {
            existingTask.setDeadline(taskDTO.getDeadline());
        }

        if (taskDTO.getCompletedDate() != null) {
            existingTask.setCompletedDate(taskDTO.getCompletedDate());
        }

        if (taskDTO.getReleaseVersionId() != null &&
                !taskDTO.getReleaseVersionId().equals(existingTask.getReleaseVersion().getId())) {
            ReleaseVersion newVersion = releaseVersionRepository.findById(taskDTO.getReleaseVersionId())
                    .orElseThrow(() -> new RuntimeException("Release Version not found"));
            existingTask.setReleaseVersion(newVersion);
        }

        if (taskDTO.getAssignedUserId() != null &&
                (existingTask.getAssignedUser() == null || !taskDTO.getAssignedUserId().equals(existingTask.getAssignedUser().getId()))) {
            User newAssignedUser = userRepository.findById(taskDTO.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned User not found"));
            existingTask.setAssignedUser(newAssignedUser);
        }

        existingTask.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public TaskDTO assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setAssignedUser(user);
        task.setAssignedDate(LocalDate.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }
    private TaskDTO convertToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        taskDTO.setReleaseVersionId(task.getReleaseVersion().getId());
        return taskDTO;
    }
}
