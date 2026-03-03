package com.spendwise.dto;

import lombok.Data;

@Data
public class RecurrentExpenseRecordDTO {

    private Long id;
    private RecurrentExpenseDTO recurrentExpense;
    private Integer month;
    private Integer year;
    private Boolean cancelled;
    private ExpenseDTO expense;

}
