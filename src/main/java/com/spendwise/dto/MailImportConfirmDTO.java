package com.spendwise.dto;

import lombok.Data;

@Data
public class MailImportConfirmDTO {

    private Long categoryId;
    private Long paymentMethodId;
    private String description;

}
