package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "project_resource_map")
@Data
public class ProjectResourceMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    @Column(length = 60000) // Stores up to 65,535 characters
    private String mapString;
}
