package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "SAVING")
@Data
public class Saving extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne
    @JoinColumn(name ="CURRENCY_ID")
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "SAVINGS_WALLET_ID")
    private SavingsWallet savingsWallet;

    @Column(name = "AMOUNT_ARS")
    private BigDecimal amountInPesos;

    @Column(name = "AMOUNT_USD")
    private BigDecimal amountInDollars;

    @Column(name = "DATE")
    private LocalDate date;


}
