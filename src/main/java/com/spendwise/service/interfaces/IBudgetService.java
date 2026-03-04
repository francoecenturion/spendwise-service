package com.spendwise.service.interfaces;

import com.spendwise.dto.BudgetDTO;
import com.spendwise.dto.BudgetFilterDTO;
import com.spendwise.model.Budget;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBudgetService {

    void populate(Budget budget, BudgetDTO dto);
    BudgetDTO create(BudgetDTO dto);
    BudgetDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<BudgetDTO> list(BudgetFilterDTO filters, Pageable pageable);
    BudgetDTO update(Long id, BudgetDTO dto) throws ChangeSetPersister.NotFoundException;
    BudgetDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    BudgetDTO enable(Long id) throws ChangeSetPersister.NotFoundException;
    BudgetDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    BudgetDTO createNextMonth() throws ChangeSetPersister.NotFoundException;

}
