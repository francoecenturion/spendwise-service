package com.spendwise.dto;

import lombok.Data;

@Data
public class GmailCredentialDTO {

    private String gmailEmail;
    private String appPassword;
    private Boolean isActive;

}
