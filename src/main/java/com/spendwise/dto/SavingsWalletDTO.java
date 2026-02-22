package com.spendwise.dto;

import lombok.Data;

@Data
public class SavingsWalletDTO {

    private Long id;
    private String name;
    private String savingsWalletType;
    private Boolean enabled;
    private String icon;
}
