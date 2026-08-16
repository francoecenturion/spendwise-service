package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HistorySummaryDTO {
    private List<YearlySummaryDTO> years;
    private BigDecimal allTimeExpensesARS;
    private BigDecimal allTimeExpensesUSD;
    private BigDecimal allTimeIncomeARS;
    private BigDecimal allTimeIncomeUSD;
}
