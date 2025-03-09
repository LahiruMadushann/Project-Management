package com.project_management.controllers;

import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.models.Notification;
import com.project_management.repositories.NotificationRespository;
import com.project_management.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private NotificationRespository notificationRespository;


    @GetMapping
    public ResponseEntity<List<Notification>> getNotification(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            String role = jwtTokenProvider.getRole(token);
            Long currentUserId = jwtTokenProvider.getUserId(token);

            if (role.equals("ROLE_ADMIN")) {
                return ResponseEntity.status(HttpStatus.OK).body(notificationRespository.findAll());
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(notificationRespository.findAllByToId(currentUserId));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(notificationRespository.findAll());
    }
}
