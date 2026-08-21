package com.spendwise.dto;

import lombok.Data;

@Data
public class MerchantShortcutDTO {

    private Long id;
    private String name;
    private Boolean enabled;
    private String icon;
    private CategoryDTO category;

}
