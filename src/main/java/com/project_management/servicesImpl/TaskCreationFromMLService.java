package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.ReleaseVersion;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.repositories.SubTaskRepository;
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

@Service
public class TaskCreationFromMLService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private UserRepository userRepository;

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
            for (TaskDetail taskDetail : taskBreakdown.getTasks()) {
                // Create main task
                Task mainTask = new Task();
                String taskName = taskDetail.getTaskName();
                if (taskName.length() > 255) {
                    taskName = taskName.substring(0, 255);
                }
                mainTask.setName(taskName);
                String taskStatus = TaskStatus.TODO.name();
                if (taskStatus.length() > 50) {
                    taskStatus = taskStatus.substring(0, 50);
                }
                mainTask.setStatus(TaskStatus.valueOf(taskStatus));
                mainTask.setTags(taskBreakdown.getMainTask());
                mainTask.setReleaseVersion(releaseVersion);
                mainTask.setCreateUserId(createUser.getId());
                mainTask.setDifficultyLevel(difficultyLevel);
                mainTask.setAssignedUser(assignedUser);
                mainTask.setAssignedDate(assignedDate);
                mainTask.setStartDate(startDate);
                mainTask.setDeadline(deadline);
                mainTask.setCompletedDate(completedDate);
                mainTask.setCreatedAt(LocalDateTime.now()); // Set the createdAt field to the current time
                mainTask.setUpdatedAt(LocalDateTime.now()); // Set the updatedAt field to the current time


                // Create the main task
                Task savedMainTask = taskRepository.saveAndFlush(mainTask);
                TaskDTO createdMainTask = this.convertTaskToDTO(savedMainTask);

                // Create subtasks
                List<SubTaskDTO> subTasks = new ArrayList<>();
                for (SubTaskDetail subTaskDetail : taskDetail.getSubtasks()) {
                    SubTaskDTO subTask = new SubTaskDTO();
                    subTask.setName(subTaskDetail.getSubtaskName());
                    subTask.setStatus(TaskStatus.TODO);
                    subTask.setTags(subTaskDetail.getTag());
                    subTask.setTaskId(savedMainTask.getId());
                    subTask.setCreateUserId(createUserId);
                    subTask.setAssignedUserId(assignedUser.getId());
                    subTask.setAssignedDate(LocalDate.now());
                    subTask.setStartDate(LocalDate.now());
                    subTask.setDeadline(LocalDate.now().plusDays(
                            (long) Math.ceil(subTaskDetail.getEstimatedHours() / 8.0)
                    ));
                    subTask.setCompletedDate(null);
//                    subTask.setC(LocalDateTime.now());
//                    subTask.setUpdatedAt(LocalDateTime.now());


                    // Set deadline based on estimated hours
                    LocalDate subTaskDeadline = LocalDate.now().plusDays(
                            (long) Math.ceil(subTaskDetail.getEstimatedHours() / 8.0)
                    );
                    subTask.setDeadline(subTaskDeadline);

                    subTasks.add(subTask);



                }

                // Update main task with subtasks
                createdMainTask.setSubTaskList(subTasks);
                TaskDTO updatedTask = taskService.updateTask(createdMainTask.getId(), createdMainTask);
                createdTasks.add(updatedTask);
            }
        }

        return createdTasks;
    }

    private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setName(task.getName());
        taskDTO.setStatus(task.getStatus());
        taskDTO.setTags(task.getTags());
        taskDTO.setReleaseVersionId(task.getReleaseVersion().getId());
        taskDTO.setCreateUserId(task.getCreateUserId());
        taskDTO.setDifficultyLevel(task.getDifficultyLevel());
        taskDTO.setAssignedUserId(task.getAssignedUser().getId());
        taskDTO.setAssignedDate(task.getAssignedDate());
        taskDTO.setStartDate(task.getStartDate());
        taskDTO.setDeadline(task.getDeadline());
        taskDTO.setCompletedDate(task.getCompletedDate());
        return taskDTO;
    }

    private SubTaskDTO convertSubTaskToDTO(SubTask subTask) {
        SubTaskDTO subTaskDTO = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, subTaskDTO);
        subTaskDTO.setTaskId(subTask.getTask().getId());
        if (subTask.getAssignedUser() != null) {
            subTaskDTO.setAssignedUserId(subTask.getAssignedUser().getId());
        }
        return subTaskDTO;
    }
}