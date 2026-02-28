package com.spendwise.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String email;
    private String name;
    private String surname;
    private Boolean enabled;
    private String password;
    private String profilePicture;

}
