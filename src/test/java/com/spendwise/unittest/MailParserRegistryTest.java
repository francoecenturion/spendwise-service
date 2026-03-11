package com.spendwise.unittest;

import com.spendwise.mail.parser.MailParser;
import com.spendwise.mail.parser.MailParserRegistry;
import com.spendwise.mail.parser.ParsedExpense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MailParserRegistry Unit Tests")
public class MailParserRegistryTest {

    // ── Stub parsers ──────────────────────────────────────────────────────────

    private static MailParser stubParser(String entity, boolean canParse) {
        return new MailParser() {
            @Override public String getEntityName() { return entity; }
            @Override public boolean canParse(String from, String subject) { return canParse; }
            @Override public ParsedExpense parse(String subject, String body) { return new ParsedExpense(); }
        };
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findParser returns the first parser that can handle the email")
    public void testFindParser_found() {
        MailParser mp = stubParser("MercadoPago", false);
        MailParser santander = stubParser("Santander", true);
        MailParserRegistry registry = new MailParserRegistry(List.of(mp, santander));

        Optional<MailParser> result = registry.findParser("noreply@santander.com.ar", "Pagaste $1.000");

        assertTrue(result.isPresent());
        assertEquals("Santander", result.get().getEntityName());
    }

    @Test
    @DisplayName("findParser returns empty when no parser matches")
    public void testFindParser_notFound() {
        MailParser mp = stubParser("MercadoPago", false);
        MailParser santander = stubParser("Santander", false);
        MailParserRegistry registry = new MailParserRegistry(List.of(mp, santander));

        Optional<MailParser> result = registry.findParser("unknown@bank.com", "Aviso desconocido");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findParser returns first matching parser when multiple parsers match")
    public void testFindParser_returnsFirst() {
        MailParser first = stubParser("First", true);
        MailParser second = stubParser("Second", true);
        MailParserRegistry registry = new MailParserRegistry(List.of(first, second));

        Optional<MailParser> result = registry.findParser("from@bank.com", "subject");

        assertTrue(result.isPresent());
        assertEquals("First", result.get().getEntityName());
    }

    @Test
    @DisplayName("findParser returns empty when parser list is empty")
    public void testFindParser_emptyRegistry() {
        MailParserRegistry registry = new MailParserRegistry(List.of());

        Optional<MailParser> result = registry.findParser("from@bank.com", "subject");

        assertFalse(result.isPresent());
    }
}
