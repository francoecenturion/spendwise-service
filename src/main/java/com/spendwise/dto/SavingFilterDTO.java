package com.spendwise.dto;

import com.spendwise.model.Currency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingFilterDTO {

    private String description;
    private BigDecimal minAmountInPesos;
    private BigDecimal maxAmountInPesos;
    private BigDecimal minAmountInDollars;
    private BigDecimal maxAmountInDollars;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long currencyId;
    private Long savingsWalletId;
}
