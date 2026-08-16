package com.spendwise.model;

import com.spendwise.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "RECOMMENDED_CATEGORY")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendedCategory extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "ICON")
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private CategoryType type;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

}
