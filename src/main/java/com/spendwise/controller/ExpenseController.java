package com.spendwise.controller;

import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.ExpenseFilterDTO;
import com.spendwise.service.interfaces.IExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final IExpenseService iExpenseService;

    @Autowired
    public ExpenseController(IExpenseService iExpenseService) {
        this.iExpenseService = iExpenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> create(@RequestBody ExpenseDTO dto) {
        ExpenseDTO expense = iExpenseService.create(dto);
        log.debug("POST to Expense Finished {}", expense);
        return ResponseEntity
                .ok(expense);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        ExpenseDTO expense = iExpenseService.findById(id);
        log.debug("GET to Expense Finished {}", expense);
        return ResponseEntity
                .ok(expense);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @ModelAttribute ExpenseFilterDTO filters,
        Pageable pageable
    ) {
        Page<ExpenseDTO> categories = iExpenseService.list(filters, pageable);
        log.debug("LIST Categories Finished");
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> update(@PathVariable Long id, @RequestBody ExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        ExpenseDTO expense = iExpenseService.update(id, dto);
        log.debug("PUT to Expense Finished {}", expense);
        return ResponseEntity
                .ok(expense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ExpenseDTO> update(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        ExpenseDTO expense = iExpenseService.delete(id);
        log.debug("DELETE to Expense Finished {}", expense);
        return ResponseEntity
                .ok(expense);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<ExpenseDTO> disable(@PathVariable Long id, ExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        ExpenseDTO expense = iExpenseService.disable(id, dto);
        log.debug("DISABLE Expense Finished {}", expense);
        return ResponseEntity
                .ok(expense);
    }
}
