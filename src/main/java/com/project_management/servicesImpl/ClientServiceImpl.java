package com.project_management.servicesImpl;

import com.project_management.dto.ClientDTO;
import com.project_management.models.Client;
import com.project_management.models.Role;
import com.project_management.models.User;
import com.project_management.repositories.ClientRepository;
import com.project_management.repositories.RoleRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.ClientService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    @Autowired
    private JavaMailSender mailSender;

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

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(clientDTO.getEmail());
            helper.setSubject("Assigned to a Project");
            helper.setFrom("devrepublic07@gmail.com");

            String htmlContent = "<html><body>"
                    + "<div style='font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px;'>"
                    + "<div style='background: #007BFF; color: white; text-align: center; padding: 15px;" +
                    " font-size: 20px; font-weight: bold;'>Project Assignment Notification</div>"
                    + "<div style='background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);'>"
                    + "<p>Dear <b>" + clientDTO.getUsername() + "</b>,</p>"
                    + "<p>I am sharing your login credentials for Project Pulse Portal.You can access the project via the portal using the credentials below:</p>"
                    + "<p>\n</p>"
                    + "<p><b>Username:</b> " + clientDTO.getUsername() + "</p>"
                    + "<p><b>Password:</b> " + clientDTO.getPassword() + "</p>"
                    + "<p>Click the button below to log in and get started:</p>"
                    + "<p><a href='https://yourportal.com/login' style='display:inline-block;padding:" +
                    "12px 20px;background:#007BFF;color:#ffffff;text-decoration:none;border-radius:5px;font-size:16px;'>Access Project</a></p>"
                    + "<p>If you have any issues, feel free to contact support.</p>"
                    + "<p>Best regards,</p>"
                    + "<p><b>Project Pulse</b></p>"
                    + "</div></div></body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return convertToDTO(clientDTO);
    }

    @Override
    public ClientDTO updateClient(Long clientId, ClientDTO clientDTO) {
        Integer userId = clientRepository.findUserIdByClientId(clientId);
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new RuntimeException("User not found"));
        if (clientDTO.getUsername() != null) {
            user.setUsername(clientDTO.getUsername());
        }
        if (clientDTO.getPassword() != null) {
            user.setPassword(clientDTO.getPassword());
        }
        if (clientDTO.getEmail() != null) {
            user.setEmail(clientDTO.getEmail());
        }

        // Save user
        User updatedUser = userRepository.save(user);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (clientDTO.getProjectId() != null) {
            client.setProjectId(clientDTO.getProjectId());
        }

        Client updatedClient = clientRepository.save(client);
        clientDTO.setUsername(updatedUser.getUsername());
        clientDTO.setEmail(updatedUser.getEmail());
        clientDTO.setProjectId(updatedClient.getProjectId());
        clientDTO.setUserId(updatedClient.getUserId());
        return convertToDTO(clientDTO);
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
