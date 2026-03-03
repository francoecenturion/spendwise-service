package com.spendwise.dto;

import lombok.Data;

@Data
public class BudgetFilterDTO {

    private String description;
    private Integer month;
    private Integer year;
    private Boolean enabled;

}
