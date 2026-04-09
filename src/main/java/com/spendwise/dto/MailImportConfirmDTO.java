package com.spendwise.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MailImportConfirmDTO {

    private Long categoryId;
    private Long paymentMethodId;
    private String description;
    private LocalDate date;

}
