package com.spendwise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlySummaryDTO {
    private int year;
    private BigDecimal expensesARS;
    private BigDecimal expensesUSD;
    private BigDecimal incomeARS;
    private BigDecimal incomeUSD;
}
