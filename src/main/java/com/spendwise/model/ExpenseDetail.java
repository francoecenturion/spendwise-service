package com.spendwise.model;

import com.spendwise.enums.CategoryType;
import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "EXPENSE_DETAIL")
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpenseDetail extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATEGORY")
    private Category category;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
