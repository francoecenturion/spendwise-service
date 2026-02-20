package com.spendwise.controller;

import com.spendwise.dto.CurrencyDTO;
import com.spendwise.dto.CurrencyFilterDTO;
import com.spendwise.service.interfaces.ICurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/currencies")
public class CurrencyController {

    private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);

    private final ICurrencyService iCurrencyService;

    @Autowired
    public CurrencyController(ICurrencyService iCurrencyService) {
        this.iCurrencyService = iCurrencyService;
    }

    @PostMapping
    public ResponseEntity<CurrencyDTO> create(@RequestBody CurrencyDTO dto) {
        CurrencyDTO currency = iCurrencyService.create(dto);
        log.debug("POST to Currency Finished {}", currency);
        return ResponseEntity
                .ok(currency);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        CurrencyDTO currency = iCurrencyService.findById(id);
        log.debug("GET to Currency Finished {}", currency);
        return ResponseEntity
                .ok(currency);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @ModelAttribute CurrencyFilterDTO filters,
        Pageable pageable
    ) {
        Page<CurrencyDTO> currencies = iCurrencyService.list(filters, pageable);
        log.debug("LIST Currencies Finished");
        return ResponseEntity.ok(currencies);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyDTO> update(@PathVariable Long id, @RequestBody CurrencyDTO dto) throws ChangeSetPersister.NotFoundException {
        CurrencyDTO currency = iCurrencyService.update(id, dto);
        log.debug("PUT to Currency Finished {}", currency);
        return ResponseEntity
                .ok(currency);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CurrencyDTO> update(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        CurrencyDTO currency = iCurrencyService.delete(id);
        log.debug("DELETE    to Currency Finished {}", currency);
        return ResponseEntity
                .ok(currency);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<CurrencyDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        CurrencyDTO currency = iCurrencyService.disable(id);
        log.debug("DISABLE Currency Finished {}", currency);
        return ResponseEntity
                .ok(currency);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<CurrencyDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        CurrencyDTO currency = iCurrencyService.enable(id);
        log.debug("ENABLE Currency Finished {}", currency);
        return ResponseEntity
                .ok(currency);
    }
}
