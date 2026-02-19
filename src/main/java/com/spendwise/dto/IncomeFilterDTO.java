package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IncomeFilterDTO {

    private Long id;
    private String description;
    private BigDecimal amountInPesos;
    private BigDecimal amountInDollars;
    private CategoryDTO source;
    private LocalDate date;

}
