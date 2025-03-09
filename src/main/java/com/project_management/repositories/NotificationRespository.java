package com.project_management.repositories;

import com.project_management.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRespository extends JpaRepository<Notification,Long> {
    List<Notification> findAllByToId(long id);
}
