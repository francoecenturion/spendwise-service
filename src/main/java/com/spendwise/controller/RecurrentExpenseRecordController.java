package com.spendwise.controller;

import com.spendwise.dto.RecurrentExpenseRecordDTO;
import com.spendwise.dto.RecurrentExpenseRecordFilterDTO;
import com.spendwise.service.interfaces.IRecurrentExpenseRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recurrent-expense-records")
public class RecurrentExpenseRecordController {

    private static final Logger log = LoggerFactory.getLogger(RecurrentExpenseRecordController.class);
    private final IRecurrentExpenseRecordService iRecurrentExpenseRecordService;

    @Autowired
    public RecurrentExpenseRecordController(IRecurrentExpenseRecordService iRecurrentExpenseRecordService) {
        this.iRecurrentExpenseRecordService = iRecurrentExpenseRecordService;
    }

    @PostMapping
    public ResponseEntity<RecurrentExpenseRecordDTO> create(@RequestBody RecurrentExpenseRecordDTO dto) {
        RecurrentExpenseRecordDTO result = iRecurrentExpenseRecordService.create(dto);
        log.debug("POST to RecurrentExpenseRecord Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurrentExpenseRecordDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecordDTO result = iRecurrentExpenseRecordService.findById(id);
        log.debug("GET to RecurrentExpenseRecord Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<RecurrentExpenseRecordDTO>> list(
            @ModelAttribute RecurrentExpenseRecordFilterDTO filters,
            Pageable pageable
    ) {
        Page<RecurrentExpenseRecordDTO> result = iRecurrentExpenseRecordService.list(filters, pageable);
        log.debug("LIST RecurrentExpenseRecords Finished");
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RecurrentExpenseRecordDTO> cancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecordDTO result = iRecurrentExpenseRecordService.cancel(id);
        log.debug("CANCEL RecurrentExpenseRecord Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/uncancel")
    public ResponseEntity<RecurrentExpenseRecordDTO> uncancel(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecordDTO result = iRecurrentExpenseRecordService.uncancel(id);
        log.debug("UNCANCEL RecurrentExpenseRecord Finished {}", result);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RecurrentExpenseRecordDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecordDTO result = iRecurrentExpenseRecordService.delete(id);
        log.debug("DELETE RecurrentExpenseRecord Finished {}", result);
        return ResponseEntity.ok(result);
    }

}
