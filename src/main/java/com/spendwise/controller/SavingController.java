package com.spendwise.controller;

import com.spendwise.dto.SavingDTO;
import com.spendwise.dto.SavingFilterDTO;
import com.spendwise.service.interfaces.ISavingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/savings")
public class SavingController {

    private static final Logger log = LoggerFactory.getLogger(SavingController.class);

    private final ISavingService iSavingService;

    @Autowired
    public SavingController(ISavingService iSavingService) {
        this.iSavingService = iSavingService;
    }

    @PostMapping
    public ResponseEntity<SavingDTO> create(@RequestBody SavingDTO dto) {
        SavingDTO saving = iSavingService.create(dto);
        log.debug("POST to Saving Finished {}", saving);
        return ResponseEntity.ok(saving);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        SavingDTO saving = iSavingService.findById(id);
        log.debug("GET to Saving Finished {}", saving);
        return ResponseEntity.ok(saving);
    }

    @GetMapping
    public ResponseEntity<Page<SavingDTO>> list(
            @ModelAttribute SavingFilterDTO filters,
            Pageable pageable
    ) {
        Page<SavingDTO> savings = iSavingService.list(filters, pageable);
        log.debug("LIST Savings Finished");
        return ResponseEntity.ok(savings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingDTO> update(@PathVariable Long id, @RequestBody SavingDTO dto) throws ChangeSetPersister.NotFoundException {
        SavingDTO saving = iSavingService.update(id, dto);
        log.debug("PUT to Saving Finished {}", saving);
        return ResponseEntity.ok(saving);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SavingDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        SavingDTO saving = iSavingService.delete(id);
        log.debug("DELETE to Saving Finished {}", saving);
        return ResponseEntity.ok(saving);
    }
}
