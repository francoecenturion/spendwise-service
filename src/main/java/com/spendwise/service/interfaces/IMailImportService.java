package com.spendwise.service.interfaces;

import com.spendwise.dto.MailImportConfirmDTO;
import com.spendwise.dto.MailImportDTO;
import com.spendwise.dto.MailImportFilterDTO;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMailImportService {

    Page<MailImportDTO> list(MailImportFilterDTO filters, Pageable pageable);
    MailImportDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    MailImportDTO confirm(Long id, MailImportConfirmDTO dto) throws ChangeSetPersister.NotFoundException;
    MailImportDTO ignore(Long id) throws ChangeSetPersister.NotFoundException;
    long getPendingCount();

}
