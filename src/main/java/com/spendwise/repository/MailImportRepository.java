package com.spendwise.repository;

import com.spendwise.enums.MailImportStatus;
import com.spendwise.model.MailImport;
import com.spendwise.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MailImportRepository extends JpaRepository<MailImport, Long>, JpaSpecificationExecutor<MailImport> {
    boolean existsByUserAndImapMessageId(User user, String imapMessageId);
    long countByUserAndStatus(User user, MailImportStatus status);
}
