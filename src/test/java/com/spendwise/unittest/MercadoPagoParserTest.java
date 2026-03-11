package com.spendwise.unittest;

import com.spendwise.mail.parser.ParsedExpense;
import com.spendwise.mail.parser.impl.MercadoPagoParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MercadoPagoParser Unit Tests")
public class MercadoPagoParserTest {

    private final MercadoPagoParser parser = new MercadoPagoParser();

    // ── canParse ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("canParse returns true for payment email from MercadoPago")
    public void testCanParse_payment() {
        assertTrue(parser.canParse("info@mercadopago.com", "Pago aprobado en McDonald's"));
    }

    @Test
    @DisplayName("canParse returns true for transfer email from MercadoPago")
    public void testCanParse_transfer() {
        assertTrue(parser.canParse("info@mercadopago.com", "Tu transferencia fue enviada"));
    }

    @Test
    @DisplayName("canParse returns false for wrong sender")
    public void testCanParse_wrongSender() {
        assertFalse(parser.canParse("noreply@santander.com.ar", "Pago aprobado en McDonald's"));
    }

    @Test
    @DisplayName("canParse returns false for unrecognized subject")
    public void testCanParse_wrongSubject() {
        assertFalse(parser.canParse("info@mercadopago.com", "Resumen de cuenta"));
    }

    @Test
    @DisplayName("canParse returns false for null inputs")
    public void testCanParse_nullInputs() {
        assertFalse(parser.canParse(null, "Pago aprobado en McDonald's"));
        assertFalse(parser.canParse("info@mercadopago.com", null));
    }

    @Test
    @DisplayName("getEntityName returns MercadoPago")
    public void testGetEntityName() {
        assertEquals("MercadoPago", parser.getEntityName());
    }

    // ── parse: pago con dinero disponible (isDebt=false) ─────────────────────

    @Test
    @DisplayName("parse pago con Dinero disponible: extracts merchant, amount, isDebt=false")
    public void testParse_pagoExpense() {
        String subject = "Pago aprobado en McDonald's";
        String body = "Pagaste $ 3.960\nMedio de pago: Dinero disponible\nFecha: 12/02/2026";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals("McDonald's", result.getMerchant());
        assertEquals(new BigDecimal("3960"), result.getAmount());
        assertEquals("$", result.getCurrencySymbol());
        assertFalse(result.isDebt());
    }

    // ── parse: pago con tarjeta crédito (isDebt=true) ────────────────────────

    @Test
    @DisplayName("parse pago con Tarjeta Crédito: isDebt=true")
    public void testParse_pagoDebt_credito() {
        String subject = "Pago aprobado en Netflix";
        String body = "Pagaste $ 10.000\nMedio de pago: Tarjeta Crédito Santander";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals("Netflix", result.getMerchant());
        assertEquals(new BigDecimal("10000"), result.getAmount());
        assertTrue(result.isDebt());
    }

    @Test
    @DisplayName("parse pago con 'redito' (sin acento): isDebt=true")
    public void testParse_pagoDebt_redito() {
        String subject = "Pago aprobado en Spotify";
        String body = "Pagaste $ 5.000\nMedio de pago: Tarjeta redito BBVA";

        ParsedExpense result = parser.parse(subject, body);

        assertTrue(result.isDebt());
    }

    @Test
    @DisplayName("parse pago: amount with comma decimal (70.000,00 → 70000.00)")
    public void testParse_pagoAmountWithCommaDecimal() {
        String subject = "Pago aprobado en Supermercado";
        String body = "Pagaste $ 70.000,00\nMedio de pago: Dinero disponible";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("70000.00"), result.getAmount());
    }

    // ── parse: transferencia ─────────────────────────────────────────────────

    @Test
    @DisplayName("parse transferencia: extracts amount and recipient (with Entidad), isDebt=false")
    public void testParse_transferencia_withEntidad() {
        String subject = "Tu transferencia fue enviada";
        String body = "Ya enviamos tu transferencia de $ 125.000\n"
                + "Nombre y apellido: Juan Perez Entidad: Banco Nacion\nCBU: 123456";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("125000"), result.getAmount());
        assertEquals("Juan Perez", result.getMerchant());
        assertFalse(result.isDebt());
    }

    @Test
    @DisplayName("parse transferencia: extracts recipient without Entidad via fallback pattern")
    public void testParse_transferencia_fallbackRecipient() {
        String subject = "Tu transferencia fue enviada";
        // No "Entidad:" in body → fallback pattern [\w\s]+ used
        // Body ends right after name to avoid greedy match capturing extra tokens
        String body = "Ya enviamos tu transferencia de $ 50.000\nNombre y apellido: Maria Lopez";

        ParsedExpense result = parser.parse(subject, body);

        assertEquals(new BigDecimal("50000"), result.getAmount());
        assertEquals("Maria Lopez", result.getMerchant());
        assertFalse(result.isDebt());
    }
}
