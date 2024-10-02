package com.project_management.dto;

import lombok.Data;

@Data
public class ReleaseVersionDTO {
    private Long id;
    private Long projectId;
    private String versionName;
    private Long createUserId;
}
