package com.spendwise.model;

import com.spendwise.enums.MailImportStatus;
import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "MAIL_IMPORT",
    uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID", "IMAP_MESSAGE_ID"})
)
@Data
public class MailImport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPENSE_ID")
    private Expense expense;

    @Column(name = "IMAP_MESSAGE_ID")
    private String imapMessageId;

    @Column(name = "SENDER_ENTITY")
    private String senderEntity;

    @Column(name = "FROM_ADDRESS")
    private String fromAddress;

    @Column(name = "SUBJECT", length = 1000)
    private String subject;

    @Column(name = "PARSED_MERCHANT")
    private String parsedMerchant;

    @Column(name = "PARSED_AMOUNT", precision = 19, scale = 4)
    private BigDecimal parsedAmount;

    @Column(name = "PARSED_CURRENCY_SYMBOL", length = 10)
    private String parsedCurrencySymbol;

    @Column(name = "PARSED_DATE")
    private LocalDate parsedDate;

    @Column(name = "PARSED_IS_DEBT")
    private Boolean parsedIsDebt;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 30)
    private MailImportStatus status;

}
