package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clientId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
