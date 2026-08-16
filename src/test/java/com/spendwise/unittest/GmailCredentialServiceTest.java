package com.spendwise.unittest;

import com.spendwise.dto.GmailCredentialDTO;
import com.spendwise.mail.imap.ImapIdleManager;
import com.spendwise.model.GmailCredential;
import com.spendwise.model.auth.User;
import com.spendwise.repository.GmailCredentialRepository;
import com.spendwise.service.GmailCredentialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailCredentialService Unit Tests")
public class GmailCredentialServiceTest {

    @Mock private GmailCredentialRepository gmailCredentialRepository;
    @Mock private ImapIdleManager imapIdleManager;

    @InjectMocks
    private GmailCredentialService gmailCredentialService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save creates new credential when none exists and starts IMAP worker")
    public void testSave_createNew() {
        GmailCredentialDTO dto = new GmailCredentialDTO();
        dto.setGmailEmail("user@gmail.com");
        dto.setAppPassword("abcd efgh ijkl mnop");

        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.empty());

        GmailCredential saved = new GmailCredential();
        saved.setGmailEmail("user@gmail.com");
        saved.setIsActive(true);
        Mockito.when(gmailCredentialRepository.save(any(GmailCredential.class))).thenReturn(saved);

        GmailCredentialDTO result = gmailCredentialService.save(dto);

        assertEquals("user@gmail.com", result.getGmailEmail());
        assertTrue(result.getIsActive());
        assertNull(result.getAppPassword(), "appPassword must never be exposed in response");
        Mockito.verify(gmailCredentialRepository).save(any(GmailCredential.class));
        Mockito.verify(imapIdleManager).startWorker(saved);
    }

    @Test
    @DisplayName("save updates existing credential and restarts IMAP worker")
    public void testSave_updateExisting() {
        GmailCredentialDTO dto = new GmailCredentialDTO();
        dto.setGmailEmail("new@gmail.com");
        dto.setAppPassword("new-password");

        GmailCredential existing = new GmailCredential();
        existing.setGmailEmail("old@gmail.com");
        existing.setIsActive(true);

        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.of(existing));

        GmailCredential updated = new GmailCredential();
        updated.setGmailEmail("new@gmail.com");
        updated.setIsActive(true);
        Mockito.when(gmailCredentialRepository.save(existing)).thenReturn(updated);

        GmailCredentialDTO result = gmailCredentialService.save(dto);

        assertEquals("new@gmail.com", result.getGmailEmail());
        Mockito.verify(gmailCredentialRepository).save(existing);
        Mockito.verify(imapIdleManager).startWorker(updated);
    }

    @Test
    @DisplayName("save sets isActive=true on the credential")
    public void testSave_setsIsActiveTrue() {
        GmailCredentialDTO dto = new GmailCredentialDTO();
        dto.setGmailEmail("user@gmail.com");
        dto.setAppPassword("pass");

        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.empty());
        Mockito.when(gmailCredentialRepository.save(any(GmailCredential.class))).thenAnswer(inv -> {
            GmailCredential c = inv.getArgument(0);
            assertTrue(Boolean.TRUE.equals(c.getIsActive()), "isActive must be set to true on save");
            return c;
        });

        gmailCredentialService.save(dto);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete stops IMAP worker and removes credential from repository")
    public void testDelete_existing() {
        GmailCredential credential = new GmailCredential();
        credential.setGmailEmail("user@gmail.com");
        credential.setIsActive(true);

        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.of(credential));

        gmailCredentialService.delete();

        Mockito.verify(imapIdleManager).stopWorker(testUser.getId());
        Mockito.verify(gmailCredentialRepository).delete(credential);
    }

    @Test
    @DisplayName("delete does nothing when no credential exists")
    public void testDelete_noCredential() {
        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.empty());

        gmailCredentialService.delete();

        Mockito.verify(imapIdleManager, Mockito.never()).stopWorker(any());
        Mockito.verify(gmailCredentialRepository, Mockito.never()).delete(any(GmailCredential.class));
    }

    // ── getStatus ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getStatus returns connected=true and email when credential exists")
    public void testGetStatus_connected() {
        GmailCredential credential = new GmailCredential();
        credential.setGmailEmail("user@gmail.com");
        credential.setAppPassword("secret");
        credential.setIsActive(true);

        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.of(credential));

        GmailCredentialDTO result = gmailCredentialService.getStatus();

        assertEquals("user@gmail.com", result.getGmailEmail());
        assertTrue(result.getIsActive());
        assertNull(result.getAppPassword(), "appPassword must never be exposed");
    }

    @Test
    @DisplayName("getStatus returns isActive=false when no credential exists")
    public void testGetStatus_notConnected() {
        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.empty());

        GmailCredentialDTO result = gmailCredentialService.getStatus();

        assertFalse(result.getIsActive());
        assertNull(result.getGmailEmail());
    }

    // ── findForCurrentUser ────────────────────────────────────────────────────

    @Test
    @DisplayName("findForCurrentUser returns credential when present")
    public void testFindForCurrentUser_present() {
        GmailCredential credential = new GmailCredential();
        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.of(credential));

        Optional<GmailCredential> result = gmailCredentialService.findForCurrentUser();

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findForCurrentUser returns empty when no credential")
    public void testFindForCurrentUser_empty() {
        Mockito.when(gmailCredentialRepository.findByUser(testUser)).thenReturn(Optional.empty());

        Optional<GmailCredential> result = gmailCredentialService.findForCurrentUser();

        assertFalse(result.isPresent());
    }
}
