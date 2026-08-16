package com.spendwise.dto;

import lombok.Data;

@Data
public class RecommendedPaymentMethodDTO {
    private Long id;
    private String name;
    private String iconUrl;
    private String paymentMethodType;
    private Long recommendedEntityId;
}
