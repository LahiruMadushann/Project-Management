package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class CriticalPathRequestDto {
    private List<TaskDTO> tasks;
}
