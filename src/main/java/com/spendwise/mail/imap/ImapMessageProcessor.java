package com.spendwise.mail.imap;

import com.spendwise.enums.MailImportStatus;
import com.spendwise.mail.parser.MailParser;
import com.spendwise.mail.parser.MailParserRegistry;
import com.spendwise.mail.parser.ParsedExpense;
import com.spendwise.model.MailImport;
import com.spendwise.model.auth.User;
import com.spendwise.repository.MailImportRepository;
import com.spendwise.service.interfaces.IMailImportService;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class ImapMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImapMessageProcessor.class);

    private final MailImportRepository mailImportRepository;
    private final MailParserRegistry parserRegistry;
    private final IMailImportService mailImportService;

    @Autowired
    public ImapMessageProcessor(MailImportRepository mailImportRepository,
                                MailParserRegistry parserRegistry,
                                IMailImportService mailImportService) {
        this.mailImportRepository = mailImportRepository;
        this.parserRegistry = parserRegistry;
        this.mailImportService = mailImportService;
    }

    public void process(Message message, User user) {
        try {
            String messageId = getMessageId(message);
            if (messageId == null) {
                log.warn("Message without Message-ID header, skipping");
                return;
            }

            // Deduplication check
            if (mailImportRepository.existsByUserAndImapMessageId(user, messageId)) {
                log.debug("Message {} already processed, skipping", messageId);
                return;
            }

            String from = message.getFrom() != null && message.getFrom().length > 0
                    ? message.getFrom()[0].toString() : "";
            String subject = message.getSubject() != null ? message.getSubject() : "";
            String body = extractTextBody(message);

            log.debug("Processing message from='{}' subject='{}'", from, subject);

            Optional<MailParser> parserOpt = parserRegistry.findParser(from, subject);

            MailImport mailImport = new MailImport();
            mailImport.setUser(user);
            mailImport.setImapMessageId(messageId);
            mailImport.setFromAddress(from);
            mailImport.setSubject(subject.length() > 1000 ? subject.substring(0, 1000) : subject);

            if (parserOpt.isPresent()) {
                MailParser parser = parserOpt.get();
                mailImport.setSenderEntity(parser.getEntityName());

                ParsedExpense parsed = parser.parse(subject, body);
                mailImport.setParsedMerchant(parsed.getMerchant());
                mailImport.setParsedAmount(parsed.getAmount());
                mailImport.setParsedCurrencySymbol(parsed.getCurrencySymbol());
                mailImport.setParsedIsDebt(parsed.isDebt());

                // Use date from parser; fall back to mail sent date; then today
                if (parsed.getDate() != null) {
                    mailImport.setParsedDate(parsed.getDate());
                } else if (message.getSentDate() != null) {
                    mailImport.setParsedDate(message.getSentDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate());
                } else {
                    mailImport.setParsedDate(java.time.LocalDate.now());
                }

                if (parsed.getCategoryId() != null) {
                    // Category found — create Expense (handled by MailImportService in confirm flow)
                    // For auto-confirmed: status=PENDING for now, confirmed by separate logic if needed
                    mailImport.setStatus(MailImportStatus.PENDING);
                    log.debug("Parser {} matched with category, saved as PENDING for review", parser.getEntityName());
                } else {
                    mailImport.setStatus(MailImportStatus.PENDING);
                    log.debug("Parser {} matched but no category, saved as PENDING", parser.getEntityName());
                }
            } else {
                // No parser matched — mark as SEEN and skip, don't persist
                message.setFlag(Flags.Flag.SEEN, true);
                log.debug("No parser found for message from='{}', skipping", from);
                return;
            }

            MailImport saved = mailImportRepository.save(mailImport);

            // Auto-confirm if this merchant was seen before
            mailImportService.autoConfirmIfBound(saved.getId());

            // Mark as SEEN to avoid reprocessing on reconnect
            message.setFlag(Flags.Flag.SEEN, true);

        } catch (Exception e) {
            log.error("Error processing IMAP message: {}", e.getMessage(), e);
        }
    }

    private String getMessageId(Message message) throws MessagingException {
        String[] headers = message.getHeader("Message-ID");
        if (headers != null && headers.length > 0) {
            return headers[0];
        }
        return null;
    }

    private String extractTextBody(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }
        if (part.isMimeType("text/html")) {
            // Fallback: use HTML if no plain text available
            String html = (String) part.getContent();
            return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        }
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            // Prefer text/plain over text/html
            String htmlFallback = null;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return (String) bodyPart.getContent();
                }
                if (bodyPart.isMimeType("text/html")) {
                    htmlFallback = (String) bodyPart.getContent();
                }
                if (bodyPart.isMimeType("multipart/*")) {
                    String nested = extractTextBody(bodyPart);
                    if (!nested.isEmpty()) return nested;
                }
            }
            if (htmlFallback != null) {
                return htmlFallback.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            }
        }
        return "";
    }

}
