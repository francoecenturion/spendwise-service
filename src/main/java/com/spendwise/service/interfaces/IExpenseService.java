package com.spendwise.service.interfaces;

import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.ExpenseFilterDTO;
import com.spendwise.model.Expense;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IExpenseService {

    void populate(Expense expense, ExpenseDTO dto);
    ExpenseDTO create(ExpenseDTO dto);
    ExpenseDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<ExpenseDTO> list(ExpenseFilterDTO filters, Pageable pageable);
    ExpenseDTO update(Long id, ExpenseDTO dto) throws ChangeSetPersister.NotFoundException;
    ExpenseDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    ExpenseDTO disable(Long id, ExpenseDTO dto) throws ChangeSetPersister.NotFoundException;

}
