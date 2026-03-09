package com.spendwise.dto;

import com.spendwise.enums.MailImportStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MailImportFilterDTO {

    private MailImportStatus status;
    private String senderEntity;
    private LocalDate startDate;
    private LocalDate endDate;

}
