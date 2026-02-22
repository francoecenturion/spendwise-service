package com.spendwise.model;

import com.spendwise.enums.SavingsWalletType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
}
