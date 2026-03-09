package com.spendwise.mail.parser;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ParsedExpense {

    private String merchant;
    private BigDecimal amount;
    private String currencySymbol;
    private LocalDate date;
    /** null = no se pudo categorizar → MailImport quedará PENDING */
    private Long categoryId;
    /** null = sin método de pago detectado */
    private Long paymentMethodId;
    /** true = pago con tarjeta de crédito → se registra como Deuda */
    private boolean debt;

}
