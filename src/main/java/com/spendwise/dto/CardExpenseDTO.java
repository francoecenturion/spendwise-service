package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardExpenseDTO {

    private Long id;
    private String description;
    private BigDecimal amountInPesos;
    private BigDecimal amountInDollars;
    private BigDecimal inputAmount;
    private LocalDate date;
    private LocalDate dueDate;
    private Boolean cancelled;
    private PaymentMethodDTO paymentMethod;
    private CurrencyDTO currency;

}
