package com.project_management.repositories;

import com.project_management.models.Client;
import com.project_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Integer findUserIdByClientId(Long clientId);
    Client findByUserId(Long userId);

}
