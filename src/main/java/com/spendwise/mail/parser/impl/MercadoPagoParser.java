package com.spendwise.mail.parser.impl;

import com.spendwise.mail.parser.MailParser;
import com.spendwise.mail.parser.ParsedExpense;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses emails from Mercado Pago (info@mercadopago.com).
 *
 * Handles two types:
 * 1. "Pago aprobado en {merchant}" — payment with money in account or credit card.
 *    If paid with Tarjeta Crédito → isDebt=true (registered as Debt).
 *    If paid with "Dinero disponible" → isDebt=false (registered as Expense).
 *
 * 2. "Tu transferencia fue enviada" — outgoing transfer.
 *    Always isDebt=false (registered as Expense, PENDING for user review).
 */
@Component
@Order(10)
public class MercadoPagoParser implements MailParser {

    private static final String ENTITY_NAME = "MercadoPago";

    private static final Pattern PAGO_MERCHANT = Pattern.compile("Pago aprobado en (.+)");
    private static final Pattern PAGO_AMOUNT   = Pattern.compile("Pagaste \\$\\s*([\\d.,]+)");
    private static final Pattern TRANSFER_AMOUNT     = Pattern.compile("transferencia de \\$\\s*([\\d.,]+)");
    private static final Pattern TRANSFER_RECIPIENT  = Pattern.compile("Nombre y apellido: (.+?) Entidad:");
    private static final Pattern TRANSFER_RECIPIENT2 = Pattern.compile("Nombre y apellido: ([\\w\\s]+)");

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public boolean canParse(String from, String subject) {
        if (from == null || subject == null) return false;
        boolean isMercadoPago = from.toLowerCase().contains("mercadopago.com");
        boolean isPayment  = subject.contains("Pago aprobado en ");
        boolean isTransfer = subject.contains("Tu transferencia fue enviada");
        return isMercadoPago && (isPayment || isTransfer);
    }

    @Override
    public ParsedExpense parse(String subject, String body) {
        ParsedExpense result = new ParsedExpense();
        result.setCurrencySymbol("$");

        if (subject.contains("Pago aprobado en ")) {
            parsePago(subject, body, result);
        } else {
            parseTransferencia(body, result);
        }
        return result;
    }

    private void parsePago(String subject, String body, ParsedExpense result) {
        // Merchant from subject: "Pago aprobado en MERCHANT_NAME"
        Matcher m = PAGO_MERCHANT.matcher(subject);
        if (m.find()) {
            result.setMerchant(m.group(1).trim());
        }

        // Amount from body: "Pagaste $ 10.000"
        Matcher am = PAGO_AMOUNT.matcher(body);
        if (am.find()) {
            result.setAmount(parseArgAmount(am.group(1)));
        }

        // Detect credit card: body contains "rédito" (Crédito) → isDebt=true
        boolean isCreditCard = body.contains("rédito") || body.contains("redito");
        result.setDebt(isCreditCard);
    }

    private void parseTransferencia(String body, ParsedExpense result) {
        // Amount from body: "Ya enviamos tu transferencia de $ 125.000"
        Matcher am = TRANSFER_AMOUNT.matcher(body);
        if (am.find()) {
            result.setAmount(parseArgAmount(am.group(1)));
        }

        // Recipient name: "Nombre y apellido: Reynolds Kyle Monroe Entidad: ..."
        Matcher nm = TRANSFER_RECIPIENT.matcher(body);
        if (nm.find()) {
            result.setMerchant(nm.group(1).trim());
        } else {
            Matcher nm2 = TRANSFER_RECIPIENT2.matcher(body);
            if (nm2.find()) {
                result.setMerchant(nm2.group(1).trim());
            }
        }

        // Transfers are expenses, not debts
        result.setDebt(false);
    }

    /**
     * Normalizes Argentine number format to Java BigDecimal.
     * Examples: "70.000,00" → 70000.00 | "10.000" → 10000 | "3.960" → 3960
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
