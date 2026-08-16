package com.spendwise.mail.imap;

import com.spendwise.model.GmailCredential;
import com.spendwise.repository.GmailCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Gestiona los workers IMAP IDLE por usuario.
 * Arranca automáticamente cuando el contexto Spring está listo.
 * Permite iniciar/detener workers dinámicamente cuando el usuario conecta/desconecta su Gmail.
 */
@Component
public class ImapIdleManager implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ImapIdleManager.class);

    private final GmailCredentialRepository credentialRepository;
    private final ImapMessageProcessor messageProcessor;
    private final ConcurrentHashMap<Long, Future<?>> activeWorkers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("imap-idle-worker");
        return t;
    });
    private boolean initialized = false;

    @Autowired
    public ImapIdleManager(GmailCredentialRepository credentialRepository,
                           ImapMessageProcessor messageProcessor) {
        this.credentialRepository = credentialRepository;
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // ContextRefreshedEvent fires more than once in some setups — guard with flag
        if (initialized) return;
        initialized = true;

        List<GmailCredential> credentials = credentialRepository.findAllByIsActiveTrue();
        log.info("ImapIdleManager starting {} worker(s) on application ready", credentials.size());
        credentials.forEach(this::startWorker);
    }

    /**
     * Starts an IMAP IDLE worker for the given credential.
     * Safe to call multiple times — stops existing worker first if present.
     */
    public void startWorker(GmailCredential credential) {
        Long userId = credential.getUser().getId();
        stopWorker(userId); // stop existing if any

        ImapIdleWorker worker = new ImapIdleWorker(credential, messageProcessor);
        Future<?> future = executor.submit(worker);
        activeWorkers.put(userId, future);
        log.info("IMAP IDLE worker started for userId={}", userId);
    }

    /**
     * Stops the IMAP IDLE worker for the given user.
     */
    public void stopWorker(Long userId) {
        Future<?> existing = activeWorkers.remove(userId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(true);
            log.info("IMAP IDLE worker stopped for userId={}", userId);
        }
    }

}
