package com.spendwise.controller;

import com.spendwise.dto.CardExpenseDTO;
import com.spendwise.dto.CardExpenseFilterDTO;
import com.spendwise.service.interfaces.ICardExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/card-expenses")
public class CardExpenseController {

    private static final Logger log = LoggerFactory.getLogger(CardExpenseController.class);

    private final ICardExpenseService service;

    public CardExpenseController(ICardExpenseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CardExpenseDTO> create(@RequestBody CardExpenseDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardExpenseDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CardExpenseDTO>> list(@ModelAttribute CardExpenseFilterDTO filters, Pageable pageable) {
        return ResponseEntity.ok(service.list(filters, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardExpenseDTO> update(@PathVariable Long id, @RequestBody CardExpenseDTO dto) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CardExpenseDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.delete(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<CardExpenseDTO> cancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PatchMapping("/{id}/uncancel")
    public ResponseEntity<CardExpenseDTO> uncancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(service.uncancel(id));
    }
}
