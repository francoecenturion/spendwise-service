package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "EXPENSE")
@Data
public class Expense extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "AMOUNT_ARS")
    private BigDecimal amountInPesos;

    @Column(name = "AMOUNT_USD")
    private BigDecimal amountInDollars;

    @Column(name = "DATE")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_METHOD_ID")
    private PaymentMethod paymentMethod;


}
