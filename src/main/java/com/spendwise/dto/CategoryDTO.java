package com.spendwise.dto;

import com.spendwise.enums.CategoryType;
import lombok.Data;

@Data
public class CategoryDTO {

    private Long id;
    private String name;
    private Boolean enabled;
    private CategoryType type;
    private String icon;

}
