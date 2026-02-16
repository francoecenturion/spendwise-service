package com.spendwise.dto;

import lombok.Data;

@Data
public class PaymentMethodFilterDTO {

    private String name;
    private String paymentMethodType;
    private Boolean enabled;

}
