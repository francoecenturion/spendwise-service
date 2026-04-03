package com.spendwise.dto;

import lombok.Data;

@Data
public class RecommendedCategoryDTO {
    private Long id;
    private String name;
    private String icon;
    private String type;
    private Integer displayOrder;
}
