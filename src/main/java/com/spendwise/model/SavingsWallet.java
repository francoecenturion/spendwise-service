package com.spendwise.model;

import com.spendwise.enums.SavingsWalletType;
import com.spendwise.model.auth.User;
import com.spendwise.model.IssuingEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "SAVINGS_WALLET")
public class SavingsWallet extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Enumerated
    @Column(name = "SAVINGS_WALLET_TYPE")
    private SavingsWalletType savingsWalletType;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "ICON")
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ISSUING_ENTITY_ID")
    private IssuingEntity issuingEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
}
