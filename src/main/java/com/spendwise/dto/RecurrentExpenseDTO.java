package com.spendwise.dto;

import com.spendwise.model.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecurrentExpenseDTO {

    private Long id;
    private String description;
    private String icon;
    private BigDecimal amountInPesos;
    private BigDecimal amountInDollars;
    private Integer dayOfMonth;
    private CategoryDTO category;
    private PaymentMethodDTO paymentMethod;
    private Currency currency;
    private Boolean enabled;

}
