package com.project_management.servicesImpl;

import com.project_management.dto.SubTaskDTO;
import com.project_management.dto.SubTaskDTONew;
import com.project_management.dto.UserBasicDTO;
import com.project_management.models.Employee;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.EmployeeRepository;
import com.project_management.repositories.SubTaskRepository;
import com.project_management.repositories.TaskRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.SubTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmployeeRepository employeeRepository;


    @Override
    public SubTaskDTO createSubTask(SubTaskDTO subTaskDTO) {
        Task parentTask = taskRepository.findById(subTaskDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("Parent Task not found"));

        SubTask subTask = new SubTask();
        BeanUtils.copyProperties(subTaskDTO, subTask, "id", "task");
        if (subTaskDTO.getAssignedUserId() != null) {
            User user = userRepository.findById(Long.valueOf(subTaskDTO.getAssignedUserId()))
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
        String role= null;
        List<SubTaskDTO> subTasks = subTaskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            role = jwtTokenProvider.getRole(token);
            String currentUserId = String.valueOf(jwtTokenProvider.getUserId(token));

            if (!role.equals("ROLE_ADMIN")) {
                return subTasks.stream()
                        .filter(task -> Optional.ofNullable(task.getAssignedUserId())
                                .map(id -> id.toString().equals(currentUserId))
                                .orElse(false))
                        .collect(Collectors.toList());

            }
        }
        return subTasks;
    }

    @Override
    public List<SubTaskDTO> getAllSubTasksByTaskId(long taskId) {
        String role= null;
        List<SubTaskDTO> subTasks = subTaskRepository.findAllByTask_id(taskId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            role = jwtTokenProvider.getRole(token);
            String currentUserId = String.valueOf(jwtTokenProvider.getUserId(token));

//            if (!role.equals("ROLE_ADMIN")) {
//                return subTasks.stream()
//                        .filter(task -> Optional.ofNullable(task.getAssignedUserId())
//                                .map(id -> id.toString().equals(currentUserId))
//                                .orElse(false))
//                        .collect(Collectors.toList());
//
//            }
        }
        return subTasks;
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
            User newAssignedUser = userRepository.findById(Long.valueOf(subTaskDTO.getAssignedUserId()))
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

    @Override
    @Transactional(readOnly = true)
    public List<SubTaskDTONew> getSubTasksByTaskId(Long taskId) {
        return subTaskRepository.findByTaskId(taskId).stream()
                .map(this::convertToDTONew)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubTaskDTONew> getSubTasksByAssignedUserId(Long userId) {
        return List.of();
    }

    private SubTaskDTONew convertToDTONew(SubTask subTask) {
        SubTaskDTONew dto = new SubTaskDTONew();
        dto.setId(subTask.getId());
        dto.setName(subTask.getName());
        dto.setRoleCategory(subTask.getRoleCategory());
        dto.setStatus(subTask.getStatus());
        dto.setTags(subTask.getTags());
        dto.setAssignedDate(subTask.getAssignedDate());
        dto.setStartDate(subTask.getStartDate());
        dto.setDeadline(subTask.getDeadline());
        dto.setCompletedDate(subTask.getCompletedDate());

        if (subTask.getAssignedUser() != null) {
            dto.setAssignedUser(convertToUserBasicDTO(subTask.getAssignedUser()));
        }

        return dto;
    }

    private UserBasicDTO convertToUserBasicDTO(User user) {
        UserBasicDTO dto = new UserBasicDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    private SubTaskDTO convertToDTO(SubTask subTask) {
        SubTaskDTO subTaskDto = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, subTaskDto);
        subTaskDto.setTaskId(subTask.getTask().getId());
        if (subTask.getAssignedUser() != null && subTask.getAssignedUser().getId() != null){
            Employee employee = employeeRepository.findByUserId(subTask.getAssignedUser().getId()).orElseThrow();
            subTaskDto.setAssignedUserId(employee.getEmployeeName());
        }
        return subTaskDto;
    }
}
