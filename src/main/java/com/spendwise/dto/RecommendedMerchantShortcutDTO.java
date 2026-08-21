package com.spendwise.dto;

import lombok.Data;

@Data
public class RecommendedMerchantShortcutDTO {
    private Long id;
    private String name;
    private String icon;
    private Long recommendedCategoryId;
    private Integer displayOrder;
}
