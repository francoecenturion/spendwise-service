package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "CURRENCY")
@Data
public class Currency extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "SYMBOL", length = 1)
    private String symbol;

    @Column(name = "ENABLED")
    private Boolean enabled;

}
