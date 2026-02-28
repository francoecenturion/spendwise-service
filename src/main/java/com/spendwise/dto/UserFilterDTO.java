package com.spendwise.dto;

import lombok.Data;

@Data
public class UserFilterDTO {

    private String name;
    private String surname;
    private String email;
    private Boolean enabled;

}
