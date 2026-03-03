package com.spendwise.dto;

import lombok.Data;

@Data
public class RecurrentExpenseRecordFilterDTO {

    private Long recurrentExpenseId;
    private Integer month;
    private Integer year;
    private Boolean cancelled;

}
