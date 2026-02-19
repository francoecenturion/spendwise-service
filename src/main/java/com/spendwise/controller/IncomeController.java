package com.spendwise.controller;

import com.spendwise.dto.IncomeDTO;
import com.spendwise.dto.IncomeFilterDTO;
import com.spendwise.service.interfaces.IIncomeService;
import com.spendwise.service.interfaces.IIncomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/income")
public class IncomeController {

    private static final Logger log = LoggerFactory.getLogger(IncomeController.class);

    private final IIncomeService iIncomeService;

    @Autowired
    public IncomeController(IIncomeService iIncomeService) {
        this.iIncomeService = iIncomeService;
    }

    @PostMapping
    public ResponseEntity<IncomeDTO> create(@RequestBody IncomeDTO dto) {
        IncomeDTO income = iIncomeService.create(dto);
        log.debug("POST to Income Finished {}", income);
        return ResponseEntity
                .ok(income);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        IncomeDTO income = iIncomeService.findById(id);
        log.debug("GET to Income Finished {}", income);
        return ResponseEntity
                .ok(income);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @ModelAttribute IncomeFilterDTO filters,
        Pageable pageable
    ) {
        Page<IncomeDTO> categories = iIncomeService.list(filters, pageable);
        log.debug("LIST Categories Finished");
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeDTO> update(@PathVariable Long id, @RequestBody IncomeDTO dto) throws ChangeSetPersister.NotFoundException {
        IncomeDTO income = iIncomeService.update(id, dto);
        log.debug("PUT to Income Finished {}", income);
        return ResponseEntity
                .ok(income);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<IncomeDTO> update(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        IncomeDTO income = iIncomeService.delete(id);
        log.debug("DELETE to Income Finished {}", income);
        return ResponseEntity
                .ok(income);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<IncomeDTO> disable(@PathVariable Long id, IncomeDTO dto) throws ChangeSetPersister.NotFoundException {
        IncomeDTO income = iIncomeService.disable(id, dto);
        log.debug("DISABLE Income Finished {}", income);
        return ResponseEntity
                .ok(income);
    }
}
