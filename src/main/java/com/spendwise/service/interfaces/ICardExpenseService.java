package com.spendwise.service.interfaces;

import com.spendwise.dto.CardExpenseDTO;
import com.spendwise.dto.CardExpenseFilterDTO;
import com.spendwise.model.CardExpense;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICardExpenseService {

    void populate(CardExpense entity, CardExpenseDTO dto);
    CardExpenseDTO create(CardExpenseDTO dto);
    CardExpenseDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<CardExpenseDTO> list(CardExpenseFilterDTO filters, Pageable pageable);
    CardExpenseDTO update(Long id, CardExpenseDTO dto) throws ChangeSetPersister.NotFoundException;
    CardExpenseDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    CardExpenseDTO cancel(Long id) throws ChangeSetPersister.NotFoundException;
    CardExpenseDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException;

}
