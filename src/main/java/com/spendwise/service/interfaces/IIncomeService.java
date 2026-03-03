package com.spendwise.service.interfaces;

import com.spendwise.dto.IncomeDTO;
import com.spendwise.dto.IncomeFilterDTO;
import com.spendwise.model.Income;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IIncomeService {

    void populate(Income income, IncomeDTO dto);
    IncomeDTO create(IncomeDTO dto);
    IncomeDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<IncomeDTO> list(IncomeFilterDTO filters, Pageable pageable);
    IncomeDTO update(Long id, IncomeDTO dto) throws ChangeSetPersister.NotFoundException;
    IncomeDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    IncomeDTO disable(Long id, IncomeDTO dto) throws ChangeSetPersister.NotFoundException;

}
