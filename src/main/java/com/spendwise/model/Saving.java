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

    @Column(name = "AMOUNT_ARS")
    private BigDecimal amountInPesos;

    @Column(name = "AMOUNT_USD")
    private BigDecimal amountInDollars;


}
