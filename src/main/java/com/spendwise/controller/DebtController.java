package com.spendwise.controller;

import com.spendwise.dto.DebtDTO;
import com.spendwise.dto.DebtFilterDTO;
import com.spendwise.service.interfaces.IDebtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debts")
public class DebtController {

    private static final Logger log = LoggerFactory.getLogger(DebtController.class);

    private final IDebtService iDebtService;

    @Autowired
    public DebtController(IDebtService iDebtService) {
        this.iDebtService = iDebtService;
    }

    @PostMapping
    public ResponseEntity<DebtDTO> create(@RequestBody DebtDTO dto) {
        DebtDTO debt = iDebtService.create(dto);
        log.debug("POST to Debt Finished {}", debt);
        return ResponseEntity.ok(debt);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebtDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        DebtDTO debt = iDebtService.findById(id);
        log.debug("GET to Debt Finished {}", debt);
        return ResponseEntity.ok(debt);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @ModelAttribute DebtFilterDTO filters,
            Pageable pageable
    ) {
        Page<DebtDTO> debts = iDebtService.list(filters, pageable);
        log.debug("LIST Debts Finished");
        return ResponseEntity.ok(debts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebtDTO> update(@PathVariable Long id, @RequestBody DebtDTO dto) throws ChangeSetPersister.NotFoundException {
        DebtDTO debt = iDebtService.update(id, dto);
        log.debug("PUT to Debt Finished {}", debt);
        return ResponseEntity.ok(debt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DebtDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        DebtDTO debt = iDebtService.delete(id);
        log.debug("DELETE to Debt Finished {}", debt);
        return ResponseEntity.ok(debt);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<DebtDTO> cancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        DebtDTO debt = iDebtService.cancel(id);
        log.debug("CANCEL Debt Finished {}", debt);
        return ResponseEntity.ok(debt);
    }

    @PatchMapping("/{id}/uncancel")
    public ResponseEntity<DebtDTO> uncancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        DebtDTO debt = iDebtService.uncancel(id);
        log.debug("UNCANCEL Debt Finished {}", debt);
        return ResponseEntity.ok(debt);
    }

}
