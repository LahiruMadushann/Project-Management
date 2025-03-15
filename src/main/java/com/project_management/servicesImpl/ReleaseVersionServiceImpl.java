package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.repositories.ClientRepository;
import com.project_management.repositories.ProjectRepository;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.repositories.UserStoryRepository;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.ReleaseVersionService;
import com.project_management.services.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReleaseVersionServiceImpl implements ReleaseVersionService {

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
        ReleaseVersion releaseVersion = releaseVersionRepository.findByProjectId(id).get(0);
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
    @Transactional(readOnly = true)
    public List<ReleaseVersionDTONew> getReleaseVersionsByProjectIdNew(Long projectId) {
        return releaseVersionRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTONew)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReleaseVersionDTO> getAllReleaseVersions() {
        String role= null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<ReleaseVersionDTO> releaseVersions = releaseVersionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            role = jwtTokenProvider.getRole(token);
            Long currentUserId = jwtTokenProvider.getUserId(token);

            if (role != null && !role.equals("ROLE_ADMIN")) {
                 return releaseVersions.stream()
                        .filter(releaseVersion -> releaseVersion.getTasks() != null)
                        .filter(releaseVersion -> releaseVersion.getTasks().stream()
                                .anyMatch(task -> task.getSubTaskList() != null &&
                                        task.getSubTaskList().stream()
                                                .anyMatch(subTask -> subTask.getAssignedUserId() != null &&
                                                        subTask.getAssignedUserId().equals(currentUserId))
                                )
                        )
                        .collect(Collectors.toList());

            }
        }

        return releaseVersions;
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

    @Override
    public UserStoryListResponseDto getUserStoriesByProjectId(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = 0L;
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            currentUserId = jwtTokenProvider.getUserId(token);
        }
        List<Integer> client = clientRepository.findProjectIdsByUserId(Math.toIntExact(currentUserId));
        Map<Long,List<UserStoryModel>> innerMap = new HashMap<>();
        Map<Long,Map<Long,List<UserStoryModel>>> outerMap = new HashMap<>();
        client.forEach(project -> {
            List<ReleaseVersionDTO> releaseVersions = getReleaseVersionsByProjectId(Long.valueOf(project));
            releaseVersions.forEach(releaseVersionDTO -> {
                List<UserStoryModel> temp = userStoryRepository.findAllByReleaseId(releaseVersionDTO.getId());
                if(!temp.isEmpty()){
                    innerMap.put(releaseVersionDTO.getId(),temp);
                }
            });
            outerMap.put(Long.valueOf(project),innerMap);
        });
        UserStoryListResponseDto response = new UserStoryListResponseDto();
        response.setUserStories(outerMap);
        return response;
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
            subTaskDTO.setAssignedUserId(String.valueOf(subTask.getAssignedUser().getId()));
        }

        return subTaskDTO;
    }

    private ReleaseVersionDTONew convertToDTONew(ReleaseVersion version) {
        ReleaseVersionDTONew dto = new ReleaseVersionDTONew();
        dto.setId(version.getId());
        dto.setVersionName(version.getVersionName());
        dto.setVersionDescription(version.getVersionDescription());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setUpdatedAt(version.getUpdatedAt());
        dto.setVersionLimitConstant(version.getVersionLimitConstant());
        dto.setTasks(taskService.getTasksByReleaseVersionId(version.getId()));
        return dto;
    }
}
