package com.spendwise.controller;

import com.spendwise.dto.PersonalDebtDTO;
import com.spendwise.dto.PersonalDebtFilterDTO;
import com.spendwise.service.interfaces.IPersonalDebtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personal-debts")
public class PersonalDebtController {

    private static final Logger log = LoggerFactory.getLogger(PersonalDebtController.class);

    private final IPersonalDebtService service;

    public PersonalDebtController(IPersonalDebtService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PersonalDebtDTO> create(@RequestBody PersonalDebtDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonalDebtDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PersonalDebtDTO>> list(@ModelAttribute PersonalDebtFilterDTO filters, Pageable pageable) {
        return ResponseEntity.ok(service.list(filters, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalDebtDTO> update(@PathVariable Long id, @RequestBody PersonalDebtDTO dto) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PersonalDebtDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.delete(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PersonalDebtDTO> cancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PatchMapping("/{id}/uncancel")
    public ResponseEntity<PersonalDebtDTO> uncancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.uncancel(id));
    }
}
