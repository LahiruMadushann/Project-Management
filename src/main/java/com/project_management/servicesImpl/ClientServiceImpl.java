package com.project_management.servicesImpl;

import com.project_management.dto.ClientDTO;
import com.project_management.models.Client;
import com.project_management.models.Role;
import com.project_management.models.User;
import com.project_management.repositories.ClientRepository;
import com.project_management.repositories.RoleRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ClientDTO createClient(ClientDTO clientDTO) {

        if (userRepository.findByUsername(clientDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Find role
        Role role = roleRepository.findByName(clientDTO.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Create new user
        User newUser = new User();
        newUser.setUsername(clientDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        newUser.setEmail(clientDTO.getEmail());
        newUser.setRole(role);

        // Save user
        userRepository.save(newUser);
        Integer userId = userRepository.findIdByUsername(clientDTO.getUsername());
        clientDTO.setUserId(userId);
        Client client = new Client();
        client.setProjectId(clientDTO.getProjectId());
        client.setUserId(clientDTO.getUserId());

        Client savedClient = clientRepository.save(client);
        clientDTO.setClientId(savedClient.getClientId());
        return convertToDTO(clientDTO);
    }

    @Override
    public ClientDTO updateClient(Long clientId, ClientDTO clientDTO) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setProjectId(clientDTO.getProjectId());
        client.setUserId(clientDTO.getUserId());

        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    @Override
    public void deleteClient(Long clientId) {
        clientRepository.deleteById(clientId);
    }

    @Override
    public ClientDTO getClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return convertToDTO(client);
    }

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO activateClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    @Override
    public ClientDTO deactivateClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    private ClientDTO convertToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setClientId(client.getClientId());
        dto.setProjectId(client.getProjectId());
        dto.setUserId(client.getUserId());
        return dto;
    }

    private ClientDTO convertToDTO(ClientDTO client) {
        ClientDTO dto = new ClientDTO();
        dto.setUsername(client.getUsername());
        dto.setClientId(client.getClientId());
        dto.setProjectId(client.getProjectId());
        dto.setUserId(client.getUserId());
        dto.setEmail(client.getEmail());
        dto.setClientId(client.getClientId());
        return dto;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
