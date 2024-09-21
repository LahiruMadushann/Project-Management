package com.project_management.services;

import com.project_management.dto.LoginResponseDTO;
import com.project_management.dto.UserDTO;

import java.util.List;

public interface AuthService {
    LoginResponseDTO login(UserDTO userDTO);
    List<String> getPermissionsByUsername(String username);
}

