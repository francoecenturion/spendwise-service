package com.spendwise.dto;

import lombok.Data;

@Data
public class SavingsWalletFilterDTO {

    private String name;
    private String savingsWalletType;
    private Boolean enabled;
}
