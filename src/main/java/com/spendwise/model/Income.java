package com.spendwise.model;

import com.spendwise.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "INCOME")
@Data
public class Income extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "AMOUNT_ARS")
    private BigDecimal amountInPesos;

    @Column(name = "AMOUNT_USD")
    private BigDecimal amountInDollars;

    @ManyToOne
    @JoinColumn(name = "SOURCE_ID")
    private Category source;

    @Column(name = "DATE")
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
