package com.project_management.models;

import com.project_management.models.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sub_tasks")
public class SubTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private String name;

    @Column(name = "create_user_id", nullable = false)
    private Long createUserId;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Enumerated(EnumType.STRING)
    @Column(length = 250)
    private TaskStatus status;

    private String tags;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    private LocalDate deadline;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}