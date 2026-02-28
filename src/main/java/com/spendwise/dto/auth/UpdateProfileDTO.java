package com.spendwise.dto.auth;

import lombok.Data;

@Data
public class UpdateProfileDTO {

    private String name;
    private String surname;
    private String profilePicture;

}
