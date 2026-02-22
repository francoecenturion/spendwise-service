package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DebtDTO {

    private Long id;
    private String description;
    private BigDecimal amountInPesos;
    private BigDecimal amountInDollars;
    private LocalDate date;
    private LocalDate dueDate;
    private Boolean cancelled;
    private Boolean personal;
    private String creditor;
    private IssuingEntityDTO issuingEntity;
    private PaymentMethodDTO paymentMethod;

}
