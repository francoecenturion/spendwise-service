package com.spendwise.model;

import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "RECURRENT_EXPENSE")
@Data
public class RecurrentExpense extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "AMOUNT_ARS")
    private BigDecimal amountInPesos;

    @Column(name = "AMOUNT_USD")
    private BigDecimal amountInDollars;

    @Column(name = "DAY_OF_MONTH")
    private Integer dayOfMonth;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_METHOD_ID")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "CURRENCY_ID")
    private Currency currency;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
