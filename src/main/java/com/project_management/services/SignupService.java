package com.project_management.services;

import com.project_management.dto.ClientDTO;
import com.project_management.dto.ClientSignUpResponse;
import com.project_management.dto.SignupRequestDTO;
import com.project_management.dto.SignupResponseDTO;

public interface SignupService {
    SignupResponseDTO signup(SignupRequestDTO signupRequestDTO);
    ClientSignUpResponse signup(ClientDTO clientDTO);
}
