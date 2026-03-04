package com.spendwise.controller;

import com.spendwise.dto.BudgetDTO;
import com.spendwise.dto.BudgetFilterDTO;
import com.spendwise.service.interfaces.IBudgetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private static final Logger log = LoggerFactory.getLogger(BudgetController.class);
    private final IBudgetService iBudgetService;

    @Autowired
    public BudgetController(IBudgetService iBudgetService) {
        this.iBudgetService = iBudgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> create(@RequestBody BudgetDTO dto) {
        BudgetDTO result = iBudgetService.create(dto);
        log.debug("POST to Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        BudgetDTO result = iBudgetService.findById(id);
        log.debug("GET to Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<BudgetDTO>> list(
            @ModelAttribute BudgetFilterDTO filters,
            Pageable pageable
    ) {
        Page<BudgetDTO> result = iBudgetService.list(filters, pageable);
        log.debug("LIST Budgets Finished");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> update(@PathVariable Long id, @RequestBody BudgetDTO dto) throws ChangeSetPersister.NotFoundException {
        BudgetDTO result = iBudgetService.update(id, dto);
        log.debug("PUT to Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BudgetDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        BudgetDTO result = iBudgetService.delete(id);
        log.debug("DELETE to Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<BudgetDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        BudgetDTO result = iBudgetService.enable(id);
        log.debug("ENABLE Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<BudgetDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        BudgetDTO result = iBudgetService.disable(id);
        log.debug("DISABLE Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/next-month")
    public ResponseEntity<BudgetDTO> createNextMonth() throws ChangeSetPersister.NotFoundException {
        BudgetDTO result = iBudgetService.createNextMonth();
        log.debug("POST /next-month Budget Finished {}", result);
        return ResponseEntity.ok(result);
    }

}
