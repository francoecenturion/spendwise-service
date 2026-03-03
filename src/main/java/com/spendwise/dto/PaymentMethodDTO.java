package com.spendwise.dto;

import lombok.Data;

@Data
public class PaymentMethodDTO {

    private Long id;
    private String name;
    private String paymentMethodType;
    private Boolean enabled;
    private String icon;
    private IssuingEntityDTO issuingEntity;
    private String brand;
}
