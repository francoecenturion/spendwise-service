package com.spendwise.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DebtFilterDTO {

    private String description;
    private Boolean cancelled;
    private Boolean personal;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmountInPesos;
    private BigDecimal maxAmountInPesos;
    private Long paymentMethodId;
    private Long issuingEntityId;

}
