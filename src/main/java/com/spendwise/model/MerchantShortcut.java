package com.spendwise.model;

import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "MERCHANT_SHORTCUT")
@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantShortcut extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "ICON", length = 2048)
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

}
