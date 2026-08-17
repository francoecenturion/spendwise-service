package com.spendwise.dto;

import lombok.Data;

@Data
public class ExpenseDetailDTO {

    private String description;
    private CategoryDTO category;
    private Boolean enabled;
    private UserDTO user;

}
