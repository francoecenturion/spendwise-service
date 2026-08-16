package com.spendwise.mail.parser.impl;

import com.spendwise.mail.parser.MailParser;
import com.spendwise.mail.parser.ParsedExpense;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses emails from Santander Argentina (mensajesyavisos@mails.santander.com.ar).
 *
 * Handles two types:
 * 1. "Pagaste $X" — credit card consumption notification.
 *    Always isDebt=true (card purchase → Debt).
 *    Extracts: amount (from subject), merchant (Comercio field), date (Fecha field).
 *
 * 2. "Aviso de transferencia" — outgoing bank transfer.
 *    isDebt=false (registered as Expense, PENDING for user review).
 *    Extracts: amount (Importe field), recipient (Destinatario field).
 */
@Component
@Order(20)
public class SantanderParser implements MailParser {

    private static final String ENTITY_NAME = "Santander";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern SUBJECT_AMOUNT   = Pattern.compile("Pagaste \\$([\\d.,]+)");
    private static final Pattern BODY_MERCHANT    = Pattern.compile("Comercio\\s+(.+?)\\s+Fecha");
    private static final Pattern BODY_DATE        = Pattern.compile("Fecha\\s+(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern BODY_IMPORTE     = Pattern.compile("Importe\\s+\\$\\s*([\\d.,]+)");
    private static final Pattern BODY_DESTINATARIO = Pattern.compile("Destinatario\\s+(\\S+)");

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public boolean canParse(String from, String subject) {
        if (from == null || subject == null) return false;
        boolean isSantander = from.toLowerCase().contains("santander.com.ar");
        boolean isPayment   = subject.startsWith("Pagaste $");
        boolean isTransfer  = subject.equals("Aviso de transferencia");
        return isSantander && (isPayment || isTransfer);
    }

    @Override
    public ParsedExpense parse(String subject, String body) {
        ParsedExpense result = new ParsedExpense();
        result.setCurrencySymbol("$");

        if (subject.startsWith("Pagaste $")) {
            parsePago(subject, body, result);
        } else {
            parseTransferencia(body, result);
        }
        return result;
    }

    private void parsePago(String subject, String body, ParsedExpense result) {
        // Amount from subject: "Pagaste $70.000,00"
        Matcher sm = SUBJECT_AMOUNT.matcher(subject);
        if (sm.find()) {
            result.setAmount(parseArgAmount(sm.group(1)));
        }

        // Merchant from body table: "Comercio RENAPER M Fecha"
        Matcher mm = BODY_MERCHANT.matcher(body);
        if (mm.find()) {
            result.setMerchant(mm.group(1).trim());
        }

        // Date from body table: "Fecha 19/02/2026"
        Matcher dm = BODY_DATE.matcher(body);
        if (dm.find()) {
            try {
                result.setDate(LocalDate.parse(dm.group(1), DATE_FMT));
            } catch (Exception ignored) {}
        }

        // Santander credit card consumptions are always debts
        result.setDebt(true);
    }

    private void parseTransferencia(String body, ParsedExpense result) {
        // Amount from body table: "Importe $ 100.000,00"
        Matcher am = BODY_IMPORTE.matcher(body);
        if (am.find()) {
            result.setAmount(parseArgAmount(am.group(1)));
        }

        // Recipient from body table: "Destinatario 20433832605"
        Matcher rm = BODY_DESTINATARIO.matcher(body);
        if (rm.find()) {
            result.setMerchant("Dest. " + rm.group(1).trim());
        }

        // Bank transfers are expenses, not debts
        result.setDebt(false);
    }

    /**
     * Normalizes Argentine number format to Java BigDecimal.
     * Examples: "70.000,00" → 70000.00 | "100.000,00" → 100000.00
     */
    private BigDecimal parseArgAmount(String raw) {
        String normalized = raw.trim().replace(".", "").replace(",", ".");
        if (normalized.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
