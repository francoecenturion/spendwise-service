package com.spendwise.service.interfaces;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.auth.AuthResponseDTO;
import com.spendwise.dto.auth.LoginRequestDTO;
import com.spendwise.dto.auth.UpdateProfileDTO;

public interface IAuthService {

    String register(UserDTO dto);
    String verifyEmail(String token);
    AuthResponseDTO login(LoginRequestDTO dto);
    UserDTO getProfile();
    UserDTO updateProfile(UpdateProfileDTO dto);
    void deleteAccount();

}
