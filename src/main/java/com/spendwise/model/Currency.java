package com.spendwise.model;

import com.spendwise.model.user.User;
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

    @Column(name = "SYMBOL", length = 1)
    private String symbol;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
