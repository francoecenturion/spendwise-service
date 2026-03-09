package com.spendwise.service.interfaces;

import com.spendwise.dto.GmailCredentialDTO;
import com.spendwise.model.GmailCredential;

import java.util.Optional;

public interface IGmailCredentialService {

    GmailCredentialDTO save(GmailCredentialDTO dto);
    void delete();
    GmailCredentialDTO getStatus();
    Optional<GmailCredential> findForCurrentUser();

}
