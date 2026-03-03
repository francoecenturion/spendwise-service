package com.spendwise.dto;

import com.spendwise.enums.CategoryType;
import lombok.Data;

@Data
public class CategoryFilterDTO {

    private String name;
    private Boolean enabled;
    private CategoryType type;

}
