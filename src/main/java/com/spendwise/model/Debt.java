package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DEBT")
@Data
public class Debt extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "AMOUNT_ARS")
    private BigDecimal amountInPesos;

    @Column(name = "AMOUNT_USD")
    private BigDecimal amountInDollars;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "CANCELLED")
    private Boolean cancelled;

    @Column(name = "PERSONAL")
    private Boolean personal;

    @Column(name = "CREDITOR")
    private String creditor;

    @ManyToOne
    @JoinColumn(name = "ISSUING_ENTITY")
    private IssuingEntity issuingEntity;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_METHOD_ID")
    private PaymentMethod paymentMethod;

}
