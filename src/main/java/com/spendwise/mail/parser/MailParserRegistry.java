package com.spendwise.mail.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Registro central de parsers de mail.
 * Spring inyecta automáticamente todos los beans que implementan MailParser.
 * El orden se controla con @Order en cada implementación.
 */
@Component
public class MailParserRegistry {

    private static final Logger log = LoggerFactory.getLogger(MailParserRegistry.class);

    private final List<MailParser> parsers;

    @Autowired
    public MailParserRegistry(List<MailParser> parsers) {
        this.parsers = parsers;
        log.info("MailParserRegistry loaded with {} parser(s): {}",
                parsers.size(),
                parsers.stream().map(MailParser::getEntityName).toList());
    }

    /**
     * Retorna el primer parser que puede manejar el mail,
     * o empty si ninguno lo reconoce.
     */
    public Optional<MailParser> findParser(String from, String subject) {
        return parsers.stream()
                .filter(p -> p.canParse(from, subject))
                .findFirst();
    }

}
