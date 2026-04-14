package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PersonalDebtFilterDTO {

    private String description;
    private Boolean cancelled;
    private String creditor;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmountInPesos;
    private BigDecimal maxAmountInPesos;

}
