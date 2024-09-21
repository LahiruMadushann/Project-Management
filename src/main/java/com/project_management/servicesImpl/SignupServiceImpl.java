package com.project_management.servicesImpl;

import com.project_management.dto.SignupRequestDTO;
import com.project_management.dto.SignupResponseDTO;
import com.project_management.models.Role;
import com.project_management.models.User;
import com.project_management.repositories.RoleRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupServiceImpl implements SignupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SignupResponseDTO signup(SignupRequestDTO signupRequestDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(signupRequestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Find role
        Role role = roleRepository.findByName(signupRequestDTO.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Create new user
        User newUser = new User();
        newUser.setUsername(signupRequestDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));
        newUser.setEmail(signupRequestDTO.getEmail());
        newUser.setRole(role);

        // Save user
        userRepository.save(newUser);

        return new SignupResponseDTO(newUser.getUsername(), role.getName(), "User registered successfully");
    }
}