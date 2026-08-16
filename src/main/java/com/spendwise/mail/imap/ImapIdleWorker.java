package com.spendwise.mail.imap;

import com.spendwise.model.GmailCredential;
import com.spendwise.model.auth.User;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.search.FlagTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Runnable que mantiene una conexión IMAP IDLE persistente para un usuario.
 * Cuando el servidor notifica la llegada de un nuevo mail, lo procesa inmediatamente.
 * Si la conexión cae, reconecta con backoff exponencial (1s → 2s → 4s … hasta 5min).
 *
 * Usa reflexión para llamar a idle() para evitar dependencia del paquete interno
 * com.sun.mail.imap (que varía entre implementaciones de Jakarta Mail).
 */
public class ImapIdleWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ImapIdleWorker.class);

    private static final String IMAP_HOST = "imap.gmail.com";
    private static final int IMAP_PORT = 993;
    private static final long MIN_BACKOFF_MS = 1_000L;
    private static final long MAX_BACKOFF_MS = 300_000L;

    private final GmailCredential credential;
    private final User user;
    private final ImapMessageProcessor processor;
    private volatile boolean shutdown = false;

    public ImapIdleWorker(GmailCredential credential, ImapMessageProcessor processor) {
        this.credential = credential;
        this.user = credential.getUser();
        this.processor = processor;
    }

    public void shutdown() {
        this.shutdown = true;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        long backoffMs = MIN_BACKOFF_MS;
        String email = credential.getGmailEmail();
        log.info("Starting IMAP IDLE worker for user {}", email);

        while (!shutdown && !Thread.currentThread().isInterrupted()) {
            Store store = null;
            Folder inbox = null;
            try {
                store = createStore(email, credential.getAppPassword());
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                backoffMs = MIN_BACKOFF_MS;

                log.info("IMAP IDLE connected for user {}", email);

                // Process any UNSEEN messages that arrived while disconnected
                processUnseenMessages(inbox);

                // Register listener for incoming messages
                final Folder finalInbox = inbox;
                inbox.addMessageCountListener(new MessageCountAdapter() {
                    @Override
                    public void messagesAdded(MessageCountEvent e) {
                        for (Message msg : e.getMessages()) {
                            processor.process(msg, user);
                        }
                    }
                });

                // IDLE loop via reflection to avoid implementation-specific imports
                Method idleMethod = inbox.getClass().getMethod("idle", boolean.class);
                while (!shutdown && !Thread.currentThread().isInterrupted() && inbox.isOpen()) {
                    idleMethod.invoke(inbox, true);
                }

            } catch (Exception e) {
                if (e instanceof InterruptedException || Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                    log.info("IMAP IDLE worker interrupted for user {}", email);
                    break;
                }
                if (!shutdown) {
                    log.warn("IMAP IDLE connection lost for user {} ({}), reconnecting in {}ms",
                            email, e.getMessage(), backoffMs);
                }
            } finally {
                closeQuietly(inbox, store);
            }

            if (!shutdown && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("IMAP IDLE worker stopped for user {}", email);
    }

    private void processUnseenMessages(Folder inbox) throws MessagingException {
        Message[] unseen = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        if (unseen.length > 0) {
            log.info("Processing {} unseen message(s) for {} after reconnect", unseen.length, credential.getGmailEmail());
            for (Message msg : unseen) {
                processor.process(msg, user);
            }
        }
    }

    private Store createStore(String email, String appPassword) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", IMAP_HOST);
        props.put("mail.imaps.port", String.valueOf(IMAP_PORT));
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.timeout", "120000");
        props.put("mail.imaps.connectiontimeout", "30000");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(IMAP_HOST, IMAP_PORT, email, appPassword);
        return store;
    }

    private void closeQuietly(Folder inbox, Store store) {
        try { if (inbox != null && inbox.isOpen()) inbox.close(false); } catch (Exception ignored) {}
        try { if (store != null && store.isConnected()) store.close(); } catch (Exception ignored) {}
    }

}
