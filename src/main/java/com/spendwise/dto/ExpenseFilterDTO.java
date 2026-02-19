package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseFilterDTO {

    private String description;
    private BigDecimal minAmountInPesos;
    private BigDecimal maxAmountInPesos;
    private BigDecimal minAmountInDollars;
    private BigDecimal maxAmountInDollars;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long categoryId;           // Para desplegable de categorías
    private Long paymentMethodId;      // Para desplegable de métodos de pago
    private Boolean isMicroExpense;

}