package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BudgetDTO {

    private Long id;
    private String description;
    private Integer month;
    private Integer year;
    private Boolean enabled;
    private List<RecurrentExpenseDTO> recurrentExpenses;

    // Calculated totals
    private BigDecimal totalExpectedARS;
    private BigDecimal totalExpectedUSD;
    private BigDecimal totalCancelledARS;
    private BigDecimal totalCancelledUSD;
    private Integer cancelledCount;
    private Integer pendingCount;

}
