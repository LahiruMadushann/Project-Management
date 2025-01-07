package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.EmployeeRepository;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.repositories.TaskRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(taskDTO.getReleaseVersionId())
                .orElseThrow(() -> new NoSuchElementException("Release Version not found"));

        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task, "id", "releaseVersion", "assignedUser");

        // Find suitable employee based on difficulty level
        if (taskDTO.getDifficultyLevel() != null) {
            Long projectId = releaseVersion.getProject().getId();

            // First try to find exact matches
            List<Employee> exactMatches = employeeRepository.findSuitableEmployees(
                    taskDTO.getDifficultyLevel(),
                    projectId
            );

            Employee selectedEmployee;
            if (!exactMatches.isEmpty()) {
                // If there are exact matches, randomly select one to distribute work evenly
                int randomIndex = (int) (Math.random() * exactMatches.size());
                selectedEmployee = exactMatches.get(randomIndex);
            } else {
                // If no exact matches, find the closest match
                List<Employee> closestMatches = employeeRepository.findClosestMatchEmployees(
                        taskDTO.getDifficultyLevel(),
                        projectId
                );

//                if (closestMatches.isEmpty()) {
//                    throw new NoSuchElementException("No suitable employees found for this task");
//                }
//
//                selectedEmployee = closestMatches.get(0); // Get the closest match
            }

//            User assignedUser = selectedEmployee.getUser();
//            task.setAssignedUser(assignedUser);
            task.setAssignedDate(LocalDate.now());
        }

        task.setReleaseVersion(releaseVersion);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

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
    public List<TaskDTO> getTasksByReleaseVersionId(Long releaseVersionId) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(releaseVersionId)
                .orElseThrow(() -> new RuntimeException("Release Version not found"));

        return taskRepository.findByReleaseVersion(releaseVersion).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
            existingTask.setStatus(taskDTO.getStatus());
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
    @Transactional
    public List<TaskDTO> createTasksFromMLAnalysis(
            MLAnalysisResponse mlAnalysis,
            Long releaseVersionId,
            Long createUserId,
            Integer difficultyLevel,
            Long assignedUserId,
            LocalDate assignedDate,
            LocalDate startDate,
            LocalDate deadline,
            LocalDate completedDate
    ) {
        List<TaskDTO> createdTasks = new ArrayList<>();

        ReleaseVersion releaseVersion = releaseVersionRepository.findById(releaseVersionId)
                .orElseThrow(() -> new RuntimeException("Release Version not found"));

        User createUser = userRepository.findById(createUserId)
                .orElseThrow(() -> new RuntimeException("Create User not found"));

        User assignedUser = userRepository.findById(assignedUserId)
                .orElseThrow(() -> new RuntimeException("Assigned User not found"));

        for (TaskBreakdown taskBreakdown : mlAnalysis.getResults()) {
            // Create main task
            TaskDTO mainTaskDTO = new TaskDTO();
            mainTaskDTO.setName(taskBreakdown.getMainTask());
            mainTaskDTO.setStatus(TaskStatus.TODO);
            mainTaskDTO.setTags(taskBreakdown.getMainTask());
            mainTaskDTO.setReleaseVersionId(releaseVersionId);
            mainTaskDTO.setCreateUserId(createUserId);
            mainTaskDTO.setDifficultyLevel(difficultyLevel);
            mainTaskDTO.setAssignedUserId(assignedUserId);
            mainTaskDTO.setAssignedDate(assignedDate);
            mainTaskDTO.setStartDate(startDate);
            mainTaskDTO.setDeadline(deadline);
            mainTaskDTO.setCompletedDate(completedDate);

            // Create the main task
            TaskDTO createdMainTask = createTask(mainTaskDTO);

            // Create subtasks
            List<SubTaskDTO> subTasks = new ArrayList<>();
            for (TaskDetail taskDetail : taskBreakdown.getTasks()) {
                for (SubTaskDetail subTaskDetail : taskDetail.getSubtasks()) {
                    SubTaskDTO subTaskDTO = new SubTaskDTO();
                    subTaskDTO.setName(subTaskDetail.getSubtaskName());
                    subTaskDTO.setStatus(TaskStatus.TODO);
                    subTaskDTO.setTags(subTaskDetail.getTag());
                    subTaskDTO.setTaskId(createdMainTask.getId());

                    // Set deadline based on estimated hours
                    LocalDate subTaskDeadline = LocalDate.now().plusDays(
                            (long) Math.ceil(subTaskDetail.getEstimatedHours() / 8.0)
                    );
                    subTaskDTO.setDeadline(subTaskDeadline);

                    subTasks.add(subTaskDTO);
                }
            }

            // Update main task with subtasks
            createdMainTask.setSubTaskList(subTasks);
            TaskDTO updatedTask = updateTask(createdMainTask.getId(), createdMainTask);
            createdTasks.add(updatedTask);
        }

        return createdTasks;
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
    public TaskDTO convertToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        taskDTO.setReleaseVersionId(task.getReleaseVersion().getId());
        if (task.getAssignedUser() != null && task.getAssignedUser().getId() != null){
            taskDTO.setAssignedUserId(task.getAssignedUser().getId());
        }
        if (task.getSubTasks() != null && !task.getSubTasks().isEmpty()) {
            List<SubTaskDTO> subTaskDTOs = task.getSubTasks().stream()
                    .map(this::convertSubTaskToDTO)
                    .collect(Collectors.toList());
            taskDTO.setSubTaskList(subTaskDTOs);
        }
        return taskDTO;
    }

    public SubTaskDTO convertSubTaskToDTO(SubTask subTask) {
        SubTaskDTO subTaskDTO = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, subTaskDTO);
        subTaskDTO.setTaskId(subTask.getTask().getId());
        if (subTask.getAssignedUser() != null) {
            subTaskDTO.setAssignedUserId(subTask.getAssignedUser().getId());
        }
        return subTaskDTO;
    }

}
