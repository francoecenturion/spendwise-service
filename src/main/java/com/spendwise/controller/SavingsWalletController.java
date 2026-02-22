package com.spendwise.controller;

import com.spendwise.dto.SavingsWalletDTO;
import com.spendwise.dto.SavingsWalletFilterDTO;
import com.spendwise.service.interfaces.ISavingsWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/savings-wallets")
public class SavingsWalletController {

    private static final Logger log = LoggerFactory.getLogger(SavingsWalletController.class);

    private final ISavingsWalletService iSavingsWalletService;

    @Autowired
    public SavingsWalletController(ISavingsWalletService iSavingsWalletService) {
        this.iSavingsWalletService = iSavingsWalletService;
    }

    @PostMapping
    public ResponseEntity<SavingsWalletDTO> create(@RequestBody SavingsWalletDTO dto) {
        SavingsWalletDTO savingsWallet = iSavingsWalletService.create(dto);
        log.debug("POST to SavingsWallet Finished {}", savingsWallet);
        return ResponseEntity.ok(savingsWallet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsWalletDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWalletDTO savingsWallet = iSavingsWalletService.findById(id);
        log.debug("GET to SavingsWallet Finished {}", savingsWallet);
        return ResponseEntity.ok(savingsWallet);
    }

    @GetMapping
    public ResponseEntity<Page<SavingsWalletDTO>> list(
            @ModelAttribute SavingsWalletFilterDTO filters,
            Pageable pageable
    ) {
        Page<SavingsWalletDTO> savingsWallets = iSavingsWalletService.list(filters, pageable);
        log.debug("LIST SavingsWallets Finished");
        return ResponseEntity.ok(savingsWallets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsWalletDTO> update(@PathVariable Long id, @RequestBody SavingsWalletDTO dto) throws ChangeSetPersister.NotFoundException {
        SavingsWalletDTO savingsWallet = iSavingsWalletService.update(id, dto);
        log.debug("PUT to SavingsWallet Finished {}", savingsWallet);
        return ResponseEntity.ok(savingsWallet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SavingsWalletDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWalletDTO savingsWallet = iSavingsWalletService.delete(id);
        log.debug("DELETE to SavingsWallet Finished {}", savingsWallet);
        return ResponseEntity.ok(savingsWallet);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<SavingsWalletDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWalletDTO savingsWallet = iSavingsWalletService.disable(id);
        log.debug("DISABLE SavingsWallet Finished {}", savingsWallet);
        return ResponseEntity.ok(savingsWallet);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<SavingsWalletDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        SavingsWalletDTO savingsWallet = iSavingsWalletService.enable(id);
        log.debug("ENABLE SavingsWallet Finished {}", savingsWallet);
        return ResponseEntity.ok(savingsWallet);
    }
}
