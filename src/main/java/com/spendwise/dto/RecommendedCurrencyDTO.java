package com.spendwise.dto;

import lombok.Data;

@Data
public class RecommendedCurrencyDTO {
    private Long id;
    private String name;
    private String symbol;
    private Integer displayOrder;
    private Boolean defaultSelected;
}
