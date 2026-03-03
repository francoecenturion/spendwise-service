package com.spendwise.controller;

import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.dto.RecurrentExpenseFilterDTO;
import com.spendwise.service.interfaces.IRecurrentExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recurrent-expenses")
public class RecurrentExpenseController {

    private static final Logger log = LoggerFactory.getLogger(RecurrentExpenseController.class);
    private final IRecurrentExpenseService iRecurrentExpenseService;

    @Autowired
    public RecurrentExpenseController(IRecurrentExpenseService iRecurrentExpenseService) {
        this.iRecurrentExpenseService = iRecurrentExpenseService;
    }

    @PostMapping
    public ResponseEntity<RecurrentExpenseDTO> create(@RequestBody RecurrentExpenseDTO dto) {
        RecurrentExpenseDTO result = iRecurrentExpenseService.create(dto);
        log.debug("POST to RecurrentExpense Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurrentExpenseDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseDTO result = iRecurrentExpenseService.findById(id);
        log.debug("GET to RecurrentExpense Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<RecurrentExpenseDTO>> list(
            @ModelAttribute RecurrentExpenseFilterDTO filters,
            Pageable pageable
    ) {
        Page<RecurrentExpenseDTO> result = iRecurrentExpenseService.list(filters, pageable);
        log.debug("LIST RecurrentExpenses Finished");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurrentExpenseDTO> update(@PathVariable Long id, @RequestBody RecurrentExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseDTO result = iRecurrentExpenseService.update(id, dto);
        log.debug("PUT to RecurrentExpense Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RecurrentExpenseDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseDTO result = iRecurrentExpenseService.delete(id);
        log.debug("DELETE to RecurrentExpense Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<RecurrentExpenseDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseDTO result = iRecurrentExpenseService.enable(id);
        log.debug("ENABLE RecurrentExpense Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<RecurrentExpenseDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseDTO result = iRecurrentExpenseService.disable(id);
        log.debug("DISABLE RecurrentExpense Finished {}", result);
        return ResponseEntity.ok(result);
    }

}
