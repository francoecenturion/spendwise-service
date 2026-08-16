package com.spendwise.dto;

import lombok.Data;


@Data
public class CurrencyDTO  {

    private Long id;
    private String name;
    private String symbol;
    private Boolean enabled;
    private Boolean isDefault;
    private String icon;

}
