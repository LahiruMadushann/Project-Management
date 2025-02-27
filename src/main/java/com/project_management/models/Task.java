package com.project_management.models;

import com.project_management.models.enums.PriorityLevel;
import com.project_management.models.enums.RoleCategory;
import com.project_management.models.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "release_version_id", nullable = false)
    private ReleaseVersion releaseVersion;

    @Column(nullable = false)
    private String name;

    @Column(name = "create_user_id", nullable = false)
    private Long createUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "t_role_category", nullable = true)
    private RoleCategory roleCategory;

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

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<SubTask> subTasks;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false)
    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM;

}
