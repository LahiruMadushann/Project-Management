package com.project_management.servicesImpl;

import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.dto.SubTaskDTO;
import com.project_management.dto.TaskDTO;
import com.project_management.models.Project;
import com.project_management.models.ReleaseVersion;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.repositories.ProjectRepository;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.services.ReleaseVersionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseVersionServiceImpl implements ReleaseVersionService {

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public ReleaseVersionDTO createReleaseVersion(ReleaseVersionDTO releaseVersionDTO) {
        Project project = projectRepository.findById(releaseVersionDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setVersionName(releaseVersionDTO.getVersionName());
        releaseVersion.setProject(project);
        releaseVersion.setCreateUserId(releaseVersionDTO.getCreateUserId());
        releaseVersion.setVersionDescription(releaseVersionDTO.getVersionDescription());
        releaseVersion.setCreatedAt(LocalDateTime.now());

        ReleaseVersion savedVersion = releaseVersionRepository.save(releaseVersion);
        return convertToDTO(savedVersion);
    }

    @Override
    public ReleaseVersionDTO getReleaseVersionById(Long id) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToDTO(releaseVersion);
    }

    @Override
    public List<ReleaseVersionDTO> getReleaseVersionsByProjectId(Long projectId) {
        List<ReleaseVersion> releaseVersions = releaseVersionRepository.findByProjectId(projectId);
        return releaseVersions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReleaseVersionDTO> getAllReleaseVersions() {
        return releaseVersionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReleaseVersionDTO updateReleaseVersion(Long id, ReleaseVersionDTO releaseVersionDTO) {
        ReleaseVersion existingVersion = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release Version not found"));

        if (releaseVersionDTO.getVersionName() != null) {
            existingVersion.setVersionName(releaseVersionDTO.getVersionName());
        }

        if (releaseVersionDTO.getProjectId() != null &&
                !releaseVersionDTO.getProjectId().equals(existingVersion.getProject().getId())) {
            Project newProject = projectRepository.findById(releaseVersionDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));
            existingVersion.setProject(newProject);
        }


        existingVersion.setUpdatedAt(LocalDateTime.now());
        ReleaseVersion updatedVersion = releaseVersionRepository.save(existingVersion);

        return convertToDTO(updatedVersion);

    }

    @Override
    public void deleteReleaseVersion(Long id) {
        releaseVersionRepository.deleteById(id);
    }

    private ReleaseVersionDTO convertToDTO(ReleaseVersion releaseVersion) {
        ReleaseVersionDTO dto = new ReleaseVersionDTO();
        dto.setId(releaseVersion.getId());
        dto.setProjectId(releaseVersion.getProject().getId());
        dto.setVersionName(releaseVersion.getVersionName());
        dto.setVersionDescription(releaseVersion.getVersionDescription());
        dto.setCreateUserId(releaseVersion.getCreateUserId());

        if (releaseVersion.getTasks() != null && !releaseVersion.getTasks().isEmpty()) {
            List<TaskDTO> taskDTOs = releaseVersion.getTasks().stream()
                    .map(this::convertTaskToDTO)
                    .collect(Collectors.toList());
            dto.setTasks(taskDTOs);
        }
        return dto;
    }

    private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        taskDTO.setReleaseVersionId(task.getReleaseVersion().getId());

        if (task.getAssignedUser() != null) {
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
