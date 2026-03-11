package com.spendwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegisterWithSetupDTO {

    // Basic registration fields
    private String name;
    private String surname;
    private String email;
    private String password;

    // Setup selections (all optional — user may skip steps)
    private List<CurrencyDTO> currencies;
    private List<Long> selectedEntityIds;
    private List<Long> selectedPaymentMethodIds;

}
