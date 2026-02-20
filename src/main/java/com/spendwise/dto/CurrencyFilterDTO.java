package com.spendwise.dto;

import lombok.Data;


@Data
public class CurrencyFilterDTO {

    private Long id;
    private String name;
    private String symbol;
    private Boolean enabled;

}
