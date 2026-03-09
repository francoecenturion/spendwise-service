package com.spendwise.mail.parser.impl;

import com.spendwise.mail.parser.MailParser;
import com.spendwise.mail.parser.ParsedExpense;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser de ejemplo / skeleton.
 * canParse() siempre retorna false — nunca procesa mails reales.
 *
 * Para agregar un parser real:
 * 1. Crear una nueva clase en este paquete (ej: BancoGaliciaParser)
 * 2. Implementar MailParser
 * 3. Anotar con @Component y @Order(N) (menor número = mayor prioridad)
 * 4. En canParse(): verificar from.contains("@bancogalicia.com.ar") o similar
 * 5. En parse(): usar regex para extraer monto, comercio, fecha del body
 */
@Component
@Order(999)
public class GenericBankParser implements MailParser {

    // Patrón de ejemplo: "$5.000,50" o "$ 5000.50"
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("\\$\\s*([\\d.]+(?:,\\d{1,2})?)");

    @Override
    public String getEntityName() {
        return "GenericBank";
    }

    @Override
    public boolean canParse(String from, String subject) {
        // Skeleton — nunca matchea. Reemplazar con lógica real en subclases.
        return false;
    }

    @Override
    public ParsedExpense parse(String subject, String body) {
        ParsedExpense result = new ParsedExpense();

        Matcher m = AMOUNT_PATTERN.matcher(body);
        if (m.find()) {
            String raw = m.group(1).replace(".", "").replace(",", ".");
            result.setAmount(new BigDecimal(raw));
        }

        result.setCurrencySymbol("$");
        result.setDate(LocalDate.now());
        result.setCategoryId(null);      // null → MailImport quedará PENDING
        result.setPaymentMethodId(null);

        return result;
    }

}
