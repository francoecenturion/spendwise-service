package com.spendwise.service;

import com.spendwise.dto.GmailCredentialDTO;
import com.spendwise.mail.imap.ImapIdleManager;
import com.spendwise.model.GmailCredential;
import com.spendwise.model.auth.User;
import com.spendwise.repository.GmailCredentialRepository;
import com.spendwise.service.interfaces.IGmailCredentialService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GmailCredentialService implements IGmailCredentialService {

    private static final Logger log = LoggerFactory.getLogger(GmailCredentialService.class);

    private final GmailCredentialRepository gmailCredentialRepository;
    private final ImapIdleManager imapIdleManager;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public GmailCredentialService(GmailCredentialRepository gmailCredentialRepository,
                                  ImapIdleManager imapIdleManager) {
        this.gmailCredentialRepository = gmailCredentialRepository;
        this.imapIdleManager = imapIdleManager;
    }

    @Transactional
    @Override
    public GmailCredentialDTO save(GmailCredentialDTO dto) {
        User user = currentUser();
        GmailCredential credential = gmailCredentialRepository.findByUser(user)
                .orElse(new GmailCredential());
        credential.setUser(user);
        credential.setGmailEmail(dto.getGmailEmail());
        credential.setAppPassword(dto.getAppPassword());
        credential.setIsActive(true);
        GmailCredential saved = gmailCredentialRepository.save(credential);
        imapIdleManager.startWorker(saved);
        log.debug("GmailCredential saved and IMAP worker started for user {}", user.getEmail());
        return toDTO(saved);
    }

    @Transactional
    @Override
    public void delete() {
        User user = currentUser();
        gmailCredentialRepository.findByUser(user).ifPresent(credential -> {
            imapIdleManager.stopWorker(user.getId());
            gmailCredentialRepository.delete(credential);
            log.debug("GmailCredential deleted and IMAP worker stopped for user {}", user.getEmail());
        });
    }

    @Override
    public GmailCredentialDTO getStatus() {
        User user = currentUser();
        return gmailCredentialRepository.findByUser(user)
                .map(this::toDTO)
                .orElseGet(() -> {
                    GmailCredentialDTO dto = new GmailCredentialDTO();
                    dto.setIsActive(false);
                    return dto;
                });
    }

    @Override
    public Optional<GmailCredential> findForCurrentUser() {
        return gmailCredentialRepository.findByUser(currentUser());
    }

    private GmailCredentialDTO toDTO(GmailCredential credential) {
        GmailCredentialDTO dto = new GmailCredentialDTO();
        dto.setGmailEmail(credential.getGmailEmail());
        dto.setIsActive(Boolean.TRUE.equals(credential.getIsActive()));
        // never expose appPassword in response
        return dto;
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
