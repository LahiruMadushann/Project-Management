package com.project_management.repositories;

import com.project_management.models.Client;
import com.project_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Integer findUserIdByClientId(Long clientId);
    Client findByUserId(Long userId);
    @Query("SELECT c.projectId FROM Client c WHERE c.userId = :userId")
    List<Integer> findProjectIdsByUserId(Integer userId);

}
