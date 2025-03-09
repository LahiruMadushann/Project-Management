package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private Long fromId;
    private Long toId;
    private String message;
    private String type;
}
