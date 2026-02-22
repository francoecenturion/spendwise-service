package com.spendwise.service.interfaces;

import com.spendwise.dto.DebtDTO;
import com.spendwise.dto.DebtFilterDTO;
import com.spendwise.model.Debt;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDebtService {

    void populate(Debt debt, DebtDTO dto);

    DebtDTO create(DebtDTO dto);

    DebtDTO findById(Long id) throws ChangeSetPersister.NotFoundException;

    Page<DebtDTO> list(DebtFilterDTO filters, Pageable pageable);

    DebtDTO update(Long id, DebtDTO dto) throws ChangeSetPersister.NotFoundException;

    DebtDTO delete(Long id) throws ChangeSetPersister.NotFoundException;

    DebtDTO cancel(Long id) throws ChangeSetPersister.NotFoundException;

    DebtDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException;

}
