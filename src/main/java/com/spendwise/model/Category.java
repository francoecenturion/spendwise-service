package com.spendwise.model;

import com.spendwise.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "CATEGORY")
@Data
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private CategoryType type;

}
