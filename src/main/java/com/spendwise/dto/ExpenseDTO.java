package com.spendwise.dto;

import com.spendwise.model.Currency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDTO {

    private Long id;
    private String description;
    private BigDecimal inputAmount;
    private BigDecimal amountInPesos;
    private BigDecimal amountInDollars;
    private LocalDate date;
    private CategoryDTO category;
    private PaymentMethodDTO paymentMethod;
    private Currency currency;
    private Boolean microExpense;

}
