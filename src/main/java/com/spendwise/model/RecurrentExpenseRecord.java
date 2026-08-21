package com.spendwise.model;

import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "RECURRENT_EXPENSE_RECORD")
@Data
public class RecurrentExpenseRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECURRENT_EXPENSE_ID")
    private RecurrentExpense recurrentExpense;

    @Column(name = "MONTH")
    private Integer month;

    @Column(name = "YEAR")
    private Integer year;

    @Column(name = "CANCELLED")
    private Boolean cancelled;

    /** Actual amount spent this month, accumulated from every matching Expense — never the shared budgeted template. */
    @Column(name = "AMOUNT_SPENT_IN_PESOS")
    private BigDecimal amountSpentInPesos;

    @Column(name = "AMOUNT_SPENT_IN_DOLLARS")
    private BigDecimal amountSpentInDollars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPENSE_ID", nullable = true)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
