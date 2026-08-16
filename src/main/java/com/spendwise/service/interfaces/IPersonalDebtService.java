package com.spendwise.service.interfaces;

import com.spendwise.dto.PersonalDebtDTO;
import com.spendwise.dto.PersonalDebtFilterDTO;
import com.spendwise.model.PersonalDebt;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPersonalDebtService {

    void populate(PersonalDebt entity, PersonalDebtDTO dto);
    PersonalDebtDTO create(PersonalDebtDTO dto);
    PersonalDebtDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<PersonalDebtDTO> list(PersonalDebtFilterDTO filters, Pageable pageable);
    PersonalDebtDTO update(Long id, PersonalDebtDTO dto) throws ChangeSetPersister.NotFoundException;
    PersonalDebtDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    PersonalDebtDTO cancel(Long id) throws ChangeSetPersister.NotFoundException;
    PersonalDebtDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException;

}
