package com.spendwise.controller;

import com.spendwise.dto.IssuingEntityDTO;
import com.spendwise.dto.IssuingEntityFilterDTO;
import com.spendwise.service.interfaces.IIssuingEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/issuing-entities")
public class IssuingEntityController {

    private static final Logger log = LoggerFactory.getLogger(IssuingEntityController.class);

    private final IIssuingEntityService iIssuingEntityService;

    @Autowired
    public IssuingEntityController(IIssuingEntityService iIssuingEntityService) {
        this.iIssuingEntityService = iIssuingEntityService;
    }

    @PostMapping
    public ResponseEntity<IssuingEntityDTO> create(@RequestBody IssuingEntityDTO dto) {
        IssuingEntityDTO issuingEntity = iIssuingEntityService.create(dto);
        log.debug("POST to IssuingEntity Finished {}", issuingEntity);
        return ResponseEntity.ok(issuingEntity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssuingEntityDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntityDTO issuingEntity = iIssuingEntityService.findById(id);
        log.debug("GET to IssuingEntity Finished {}", issuingEntity);
        return ResponseEntity.ok(issuingEntity);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @ModelAttribute IssuingEntityFilterDTO filters,
            Pageable pageable
    ) {
        Page<IssuingEntityDTO> issuingEntities = iIssuingEntityService.list(filters, pageable);
        log.debug("LIST IssuingEntities Finished");
        return ResponseEntity.ok(issuingEntities);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssuingEntityDTO> update(@PathVariable Long id, @RequestBody IssuingEntityDTO dto) throws ChangeSetPersister.NotFoundException {
        IssuingEntityDTO issuingEntity = iIssuingEntityService.update(id, dto);
        log.debug("PUT to IssuingEntity Finished {}", issuingEntity);
        return ResponseEntity.ok(issuingEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<IssuingEntityDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntityDTO issuingEntity = iIssuingEntityService.delete(id);
        log.debug("DELETE to IssuingEntity Finished {}", issuingEntity);
        return ResponseEntity.ok(issuingEntity);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<IssuingEntityDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntityDTO issuingEntity = iIssuingEntityService.disable(id);
        log.debug("DISABLE IssuingEntity Finished {}", issuingEntity);
        return ResponseEntity.ok(issuingEntity);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<IssuingEntityDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntityDTO issuingEntity = iIssuingEntityService.enable(id);
        log.debug("ENABLE IssuingEntity Finished {}", issuingEntity);
        return ResponseEntity.ok(issuingEntity);
    }

}
