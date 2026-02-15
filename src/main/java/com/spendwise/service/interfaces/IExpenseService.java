package com.spendwise.service.interfaces;

import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.model.Expense;
import com.spendwise.model.PaymentMethod;

import java.util.List;

public interface IExpenseService {

    void populate(Expense expense, ExpenseDTO dto);
    ExpenseDTO create(ExpenseDTO dto);
    ExpenseDTO findById(Long id);
    List<ExpenseDTO> list();
    ExpenseDTO update(Long id, ExpenseDTO dto);
    ExpenseDTO delete(Long id);
    ExpenseDTO disable(Long id);

}
