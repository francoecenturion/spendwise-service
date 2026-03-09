package com.spendwise.dto;

import com.spendwise.enums.MailImportStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MailImportDTO {

    private Long id;
    private String imapMessageId;
    private String senderEntity;
    private String fromAddress;
    private String subject;
    private String parsedMerchant;
    private BigDecimal parsedAmount;
    private String parsedCurrencySymbol;
    private LocalDate parsedDate;
    private Boolean parsedIsDebt;
    private MailImportStatus status;
    private ExpenseDTO expense;
    private LocalDateTime creationDate;

}
