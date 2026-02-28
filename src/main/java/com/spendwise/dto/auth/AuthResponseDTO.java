package com.spendwise.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String email;
    private String name;
    private String surname;
    private String profilePicture;

}
