package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Entity
@Table(name = "perfect_roles")
public class PerfectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;
}
