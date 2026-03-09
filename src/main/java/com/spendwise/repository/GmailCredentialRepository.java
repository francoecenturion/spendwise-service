package com.spendwise.repository;

import com.spendwise.model.GmailCredential;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GmailCredentialRepository extends JpaRepository<GmailCredential, Long> {
    Optional<GmailCredential> findByUser(User user);
    List<GmailCredential> findAllByIsActiveTrue();
}
