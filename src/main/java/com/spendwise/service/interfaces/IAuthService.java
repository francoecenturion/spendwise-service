package com.spendwise.service.interfaces;

import com.spendwise.dto.RegisterWithSetupDTO;
import com.spendwise.dto.UserDTO;
import com.spendwise.dto.auth.AuthResponseDTO;
import com.spendwise.dto.auth.LoginRequestDTO;
import com.spendwise.dto.auth.UpdateProfileDTO;

public interface IAuthService {

    String register(RegisterWithSetupDTO dto);
    String verifyEmail(String token);
    AuthResponseDTO login(LoginRequestDTO dto);
    AuthResponseDTO refresh(String refreshToken);
    void logout(String refreshToken);
    UserDTO getProfile();
    UserDTO updateProfile(UpdateProfileDTO dto);
    void deleteAccount();
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);

}
