package com.spendwise.service.interfaces;

import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.dto.RecurrentExpenseFilterDTO;
import com.spendwise.model.RecurrentExpense;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRecurrentExpenseService {

    void populate(RecurrentExpense recurrentExpense, RecurrentExpenseDTO dto);
    RecurrentExpenseDTO create(RecurrentExpenseDTO dto);
    RecurrentExpenseDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<RecurrentExpenseDTO> list(RecurrentExpenseFilterDTO filters, Pageable pageable);
    RecurrentExpenseDTO update(Long id, RecurrentExpenseDTO dto) throws ChangeSetPersister.NotFoundException;
    RecurrentExpenseDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    RecurrentExpenseDTO enable(Long id) throws ChangeSetPersister.NotFoundException;
    RecurrentExpenseDTO disable(Long id) throws ChangeSetPersister.NotFoundException;

}
