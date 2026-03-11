package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "RECOMMENDED_CURRENCY")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendedCurrency extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "SYMBOL", length = 20)
    private String symbol;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "DEFAULT_SELECTED")
    private Boolean defaultSelected;

}
