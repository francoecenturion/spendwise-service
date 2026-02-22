package com.spendwise.dto;

import com.spendwise.model.Currency;
import com.spendwise.model.SavingsWallet;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingDTO {

    private Long id;
    private String description;
    private Currency currency;
    private SavingsWallet savingsWallet;
    private BigDecimal inputAmount;
    private BigDecimal amountInPesos;
    private BigDecimal amountInDollars;
    private LocalDate date;

}
