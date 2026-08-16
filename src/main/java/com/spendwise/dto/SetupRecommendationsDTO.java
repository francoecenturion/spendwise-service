package com.spendwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class SetupRecommendationsDTO {
    private List<RecommendedCurrencyDTO> currencies;
    private List<RecommendedEntityDTO> entities;
    private List<RecommendedPaymentMethodDTO> paymentMethods;
}
