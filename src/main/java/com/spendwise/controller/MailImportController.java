package com.spendwise.controller;

import com.spendwise.dto.MailImportConfirmDTO;
import com.spendwise.dto.MailImportDTO;
import com.spendwise.dto.MailImportFilterDTO;
import com.spendwise.dto.MerchantBindingDTO;
import com.spendwise.service.interfaces.IMailImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mail/imports")
public class MailImportController {

    private static final Logger log = LoggerFactory.getLogger(MailImportController.class);

    private final IMailImportService mailImportService;

    @Autowired
    public MailImportController(IMailImportService mailImportService) {
        this.mailImportService = mailImportService;
    }

    @GetMapping
    public ResponseEntity<Page<MailImportDTO>> list(
            @ModelAttribute MailImportFilterDTO filters,
            Pageable pageable) {
        Page<MailImportDTO> result = mailImportService.list(filters, pageable);
        log.debug("GET /mail/imports");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MailImportDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(mailImportService.findById(id));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<MailImportDTO> confirm(
            @PathVariable Long id,
            @RequestBody MailImportConfirmDTO dto) throws ChangeSetPersister.NotFoundException {
        MailImportDTO result = mailImportService.confirm(id, dto);
        log.debug("POST /mail/imports/{}/confirm", id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/ignore")
    public ResponseEntity<MailImportDTO> ignore(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        MailImportDTO result = mailImportService.ignore(id);
        log.debug("POST /mail/imports/{}/ignore", id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", mailImportService.getPendingCount()));
    }

    @GetMapping("/binding")
    public ResponseEntity<MerchantBindingDTO> lookupBinding(@RequestParam String merchant) {
        MerchantBindingDTO result = mailImportService.lookupBinding(merchant);
        if (result == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(result);
    }

}
