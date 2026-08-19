package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecurrentExpenseRecordDTO {

    private Long id;
    private RecurrentExpenseDTO recurrentExpense;
    private Integer month;
    private Integer year;
    private Boolean cancelled;
    private BigDecimal amountSpentInPesos;
    private BigDecimal amountSpentInDollars;
    private ExpenseDTO expense;

}
