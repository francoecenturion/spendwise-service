package com.spendwise.controller;

import com.spendwise.dto.MerchantShortcutDTO;
import com.spendwise.dto.MerchantShortcutFilterDTO;
import com.spendwise.service.interfaces.IMerchantShortcutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant-shortcuts")
public class MerchantShortcutController {

    private static final Logger log = LoggerFactory.getLogger(MerchantShortcutController.class);

    private final IMerchantShortcutService iMerchantShortcutService;

    @Autowired
    public MerchantShortcutController(IMerchantShortcutService iMerchantShortcutService) {
        this.iMerchantShortcutService = iMerchantShortcutService;
    }

    @PostMapping
    public ResponseEntity<MerchantShortcutDTO> create(@RequestBody MerchantShortcutDTO dto) {
        MerchantShortcutDTO merchantShortcut = iMerchantShortcutService.create(dto);
        log.debug("POST to MerchantShortcut Finished {}", merchantShortcut);
        return ResponseEntity
                .ok(merchantShortcut);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantShortcutDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcutDTO merchantShortcut = iMerchantShortcutService.findById(id);
        log.debug("GET to MerchantShortcut Finished {}", merchantShortcut);
        return ResponseEntity
                .ok(merchantShortcut);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @ModelAttribute MerchantShortcutFilterDTO filters,
        Pageable pageable
    ) {
        Page<MerchantShortcutDTO> merchantShortcuts = iMerchantShortcutService.list(filters, pageable);
        log.debug("LIST MerchantShortcuts Finished");
        return ResponseEntity.ok(merchantShortcuts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MerchantShortcutDTO> update(@PathVariable Long id, @RequestBody MerchantShortcutDTO dto) throws ChangeSetPersister.NotFoundException {
        MerchantShortcutDTO merchantShortcut = iMerchantShortcutService.update(id, dto);
        log.debug("PUT to MerchantShortcut Finished {}", merchantShortcut);
        return ResponseEntity
                .ok(merchantShortcut);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MerchantShortcutDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcutDTO merchantShortcut = iMerchantShortcutService.delete(id);
        log.debug("DELETE to MerchantShortcut Finished {}", merchantShortcut);
        return ResponseEntity
                .ok(merchantShortcut);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<MerchantShortcutDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcutDTO merchantShortcut = iMerchantShortcutService.disable(id);
        log.debug("DISABLE MerchantShortcut Finished {}", merchantShortcut);
        return ResponseEntity
                .ok(merchantShortcut);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<MerchantShortcutDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcutDTO merchantShortcut = iMerchantShortcutService.enable(id);
        log.debug("ENABLE MerchantShortcut Finished {}", merchantShortcut);
        return ResponseEntity
                .ok(merchantShortcut);
    }
}
