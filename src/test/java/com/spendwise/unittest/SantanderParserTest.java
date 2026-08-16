package com.spendwise.unittest;

import com.spendwise.mail.parser.ParsedExpense;
import com.spendwise.mail.parser.impl.SantanderParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SantanderParser Unit Tests")
public class SantanderParserTest {

    private final SantanderParser parser = new SantanderParser();

    // ── canParse ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("canParse returns true for credit card payment email from Santander")
    public void testCanParse_payment() {
        assertTrue(parser.canParse("mensajesyavisos@mails.santander.com.ar", "Pagaste $70.000,00"));
    }

    @Test
    @DisplayName("canParse returns true for transfer email from Santander")
    public void testCanParse_transfer() {
        assertTrue(parser.canParse("mensajesyavisos@mails.santander.com.ar", "Aviso de transferencia"));
    }

    @Test
    @DisplayName("canParse returns false for wrong sender")
    public void testCanParse_wrongSender() {
        assertFalse(parser.canParse("info@mercadopago.com", "Pagaste $70.000,00"));
    }

    @Test
    @DisplayName("canParse returns false for unrecognized subject")
    public void testCanParse_wrongSubject() {
        assertFalse(parser.canParse("mensajesyavisos@mails.santander.com.ar", "Resumen de cuenta"));
    }

    @Test
    @DisplayName("canParse returns false for null inputs")
    public void testCanParse_nullInputs() {
        assertFalse(parser.canParse(null, "Pagaste $1.000"));
        assertFalse(parser.canParse("mensajesyavisos@mails.santander.com.ar", null));
    }

    @Test
    @DisplayName("getEntityName returns Santander")
    public void testGetEntityName() {
        assertEquals("Santander", parser.getEntityName());
    }

    // ── parse: pago con tarjeta crédito ──────────────────────────────────────

    @Test
    @DisplayName("parse pago: extracts amount, merchant and date; isDebt=true")
    public void testParse_pago() {
        String subject = "Pagaste $70.000,00";
        String body = "Comercio RENAPER M Fecha 19/02/2026\nTarjeta: **** 1234";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("70000.00"), result.getAmount());
        assertEquals("RENAPER M", result.getMerchant());
        assertEquals(LocalDate.of(2026, 2, 19), result.getDate());
        assertTrue(result.isDebt());
        assertEquals("$", result.getCurrencySymbol());
    }

    @Test
    @DisplayName("parse pago: amount without decimals (3.960 → 3960)")
    public void testParse_pago_amountNoDecimals() {
        String subject = "Pagaste $3.960";
        String body = "Comercio SPOTIFY Fecha 01/03/2026";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("3960"), result.getAmount());
        assertEquals("SPOTIFY", result.getMerchant());
    }

    @Test
    @DisplayName("parse pago: missing merchant leaves merchant null")
    public void testParse_pago_noMerchant() {
        String subject = "Pagaste $1.000";
        String body = "Sin datos de comercio";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("1000"), result.getAmount());
        assertNull(result.getMerchant());
        assertTrue(result.isDebt());
    }

    @Test
    @DisplayName("parse pago: invalid date is ignored, date remains null")
    public void testParse_pago_invalidDate() {
        String subject = "Pagaste $1.000";
        String body = "Comercio TEST Fecha 99/99/9999";

        ParsedExpense result = parser.parse(subject, body);

        assertNull(result.getDate());
    }

    // ── parse: transferencia ─────────────────────────────────────────────────

    @Test
    @DisplayName("parse transferencia: extracts amount and recipient; isDebt=false")
    public void testParse_transferencia() {
        String subject = "Aviso de transferencia";
        String body = "Importe $ 100.000,00\nDestinatario 20433832605\nBanco: Galicia";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("100000.00"), result.getAmount());
        assertEquals("Dest. 20433832605", result.getMerchant());
        assertFalse(result.isDebt());
        assertEquals("$", result.getCurrencySymbol());
    }

    @Test
    @DisplayName("parse transferencia: missing recipient leaves merchant null")
    public void testParse_transferencia_noRecipient() {
        String subject = "Aviso de transferencia";
        String body = "Importe $ 50.000,00";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("50000.00"), result.getAmount());
        assertNull(result.getMerchant());
        assertFalse(result.isDebt());
    }
}
