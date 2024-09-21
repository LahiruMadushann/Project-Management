package com.project_management.servicesImpl;

import com.project_management.dto.ClientDTO;
import com.project_management.models.Client;
import com.project_management.models.User;
import com.project_management.repositories.ClientRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ClientDTO createClient(ClientDTO clientDTO) {
        User currentUser = getCurrentUser();

        Client client = new Client();
        client.setClientName(clientDTO.getClientName());
        client.setCreatedBy(currentUser);
        client.setActive(true);

        Client savedClient = clientRepository.save(client);
        return convertToDTO(savedClient);
    }

    @Override
    public ClientDTO updateClient(Long clientId, ClientDTO clientDTO) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setClientName(clientDTO.getClientName());

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
        client.setActive(true);
        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    @Override
    public ClientDTO deactivateClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        client.setActive(false);
        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    private ClientDTO convertToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setClientId(client.getClientId());
        dto.setClientName(client.getClientName());
        dto.setCreatedByUsername(client.getCreatedBy().getUsername());
        dto.setActive(client.isActive());
        return dto;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
