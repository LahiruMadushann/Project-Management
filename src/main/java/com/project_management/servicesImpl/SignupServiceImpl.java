package com.project_management.servicesImpl;

import com.project_management.dto.ClientDTO;
import com.project_management.dto.ClientSignUpResponse;
import com.project_management.dto.SignupRequestDTO;
import com.project_management.dto.SignupResponseDTO;
import com.project_management.models.Client;
import com.project_management.models.Role;
import com.project_management.models.User;
import com.project_management.repositories.ClientRepository;
import com.project_management.repositories.RoleRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private ClientRepository clientRepository;

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
        Integer userId = userRepository.findIdByUsername(signupRequestDTO.getUsername());

        return new SignupResponseDTO(userId,newUser.getUsername(), role.getName(), "User registered successfully");
    }

    @Override
    public ClientSignUpResponse signup(ClientDTO clientDTO) {
//        if (clientRepository.findByClientName(clientDTO.getClientName()).isPresent()) {
//            throw new RuntimeException("Client name already exists");
//        }
//
//        // Create new client
//        User currentUser = getCurrentUser();
//        Client client = new Client();
//        client.setClientName(clientDTO.getClientName());
//        client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
//        client.setEmail(clientDTO.getEmail());
//        client.setCreatedBy(currentUser);
//        client.setActive(true);
//
//        // Save client
//        clientRepository.save(client);
//
//        return new ClientSignUpResponse(client.getClientName(),"Client registered successfully");
        return null;
    }
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}