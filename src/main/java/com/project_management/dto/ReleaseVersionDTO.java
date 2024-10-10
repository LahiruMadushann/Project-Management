package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReleaseVersionDTO {
    private Long id;
    private Long projectId;
    private String versionName;
    private Long createUserId;
    private String versionDescription;
    private List<TaskDTO> tasks;
}
