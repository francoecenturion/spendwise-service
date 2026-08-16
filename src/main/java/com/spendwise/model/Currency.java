package com.spendwise.model;

import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "CURRENCY")
@Data
public class Currency extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "SYMBOL", length = 3)
    private String symbol;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "IS_DEFAULT")
    private Boolean isDefault;

    @Column(name = "ICON", length = 2048)
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
