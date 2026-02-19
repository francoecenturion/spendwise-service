package com.spendwise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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

    @Column(name = "INCOME")
    private Boolean isIncome;

}
