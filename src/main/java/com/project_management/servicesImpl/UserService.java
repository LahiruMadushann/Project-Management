package com.project_management.servicesImpl;

import com.project_management.models.RoleAccess;
import com.project_management.models.User;
import com.project_management.repositories.RoleAccessRepository;
import com.project_management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.project_management.models.enums.Permissions.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleAccessRepository roleAccessRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<String> authorities = roleAccessRepository.findByRoleId(user.getRole().getId())
                .stream()
                .map(RoleAccess::getPermissionName)
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public void adminPermission() {
        String username = getCurrentUsername();
        List<String> permissions = getPermissionsByUsername(username);

        if (!permissions.contains(String.valueOf(ALL))) {
            throw new AccessDeniedException("You don't have permission to admin users");
        }
    }
    public void managerPermission() {
        String username = getCurrentUsername();
        List<String> permissions = getPermissionsByUsername(username);

        if (!permissions.contains(String.valueOf(MANAGER)) && !permissions.contains(String.valueOf(ALL))) {
            throw new AccessDeniedException("You don't have permission to manager users");
        }
    }
    public void employeePermission() {
        String username = getCurrentUsername();
        List<String> permissions = getPermissionsByUsername(username);

        if (!permissions.contains(String.valueOf(EMPLOYEE)) && !permissions.contains(String.valueOf(ALL))) {
            throw new AccessDeniedException("You don't have permission to employee users");
        }
    }

    public List<String> getPermissionsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return roleAccessRepository.findByRoleId(user.getRole().getId())
                .stream()
                .map(RoleAccess::getPermissionName)
                .collect(Collectors.toList());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
