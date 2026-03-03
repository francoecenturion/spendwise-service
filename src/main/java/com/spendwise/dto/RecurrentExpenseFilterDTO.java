package com.spendwise.dto;

import lombok.Data;

@Data
public class RecurrentExpenseFilterDTO {

    private String description;
    private Long categoryId;
    private Long paymentMethodId;
    private Boolean enabled;

}
