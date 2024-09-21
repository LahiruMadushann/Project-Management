package com.project_management.servicesImpl;

import com.project_management.dto.LoginResponseDTO;
import com.project_management.dto.UserDTO;
import com.project_management.models.RoleAccess;
import com.project_management.models.User;
import com.project_management.repositories.RoleAccessRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleAccessRepository roleAccessRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponseDTO login(UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordMatches(userDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole().getName());

        // Fetch role-based accesses
        List<String> permissions = getPermissionsByUsername(user.getUsername());

        return new LoginResponseDTO(token, user.getRole().getName(), permissions);
    }

    // Implement the getPermissionsByUsername logic
    @Override
    public List<String> getPermissionsByUsername(String username) {
        // Fetch user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch role-based accesses
        List<RoleAccess> accesses = roleAccessRepository.findByRoleId(user.getRole().getId());

        // Extract permission names
        return accesses.stream()
                .map(RoleAccess::getPermissionName)
                .collect(Collectors.toList());
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
    }
}
