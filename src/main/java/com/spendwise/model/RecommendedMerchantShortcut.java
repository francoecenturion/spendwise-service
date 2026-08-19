package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "RECOMMENDED_MERCHANT_SHORTCUT")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendedMerchantShortcut extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "ICON")
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECOMMENDED_CATEGORY_ID")
    private RecommendedCategory category;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

}
